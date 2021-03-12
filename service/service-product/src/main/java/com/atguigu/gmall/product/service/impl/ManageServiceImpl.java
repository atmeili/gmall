package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.MqConst;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.service.RabbitService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author mqx
 * @date 2021-1-30 14:37:45
 */
@Service
public class ManageServiceImpl implements ManageService {

    //  引入 mapper 层！
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitService rabbitService;

    @Override
    public List<BaseCategory1> getCategory1() {
        //  select * from base_category1;
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        //  select * from base_category2 where category1_id = category1Id;
        //  构建查询条件
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id",category1Id));
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        //  select * from base_category3 where category2_id = category2Id;
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }

    /**
     * 根据分类Id 查询数据 {包含平台属性+平台属性值}
     * @param category1Id   一级分类Id
     * @param category2Id   二级分类Id
     * @param category3Id   三级分类Id
     * @return
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //  设置到多表关联查询
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) //  严格：需要 (rollbackFor = Exception.class)好处是：如果不是运行时异常也能回滚！  如果有错误，需要回滚异常！
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //  什么情况下走保存，什么情况下走修改？
        //  baseAttrInfo;
        if (baseAttrInfo.getId()!=null){
            //  修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            //  保存的表
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //  baseAttrValue; 不能确定用户到底要修改哪一个数据！
        //  先将原有的数据删除{根据attrId}，然后再插入新的数据！修改方案！
        //  delete from base_attr_value where attr_id = baseAttrInfo.getId();
        //  base_attr_value.attr_id = base_attr_info.id 一样的！
        //  将平台属性对应的 属性值全部清空
        baseAttrValueMapper.delete(new QueryWrapper<BaseAttrValue>().eq("attr_id",baseAttrInfo.getId()));

        //  先获取到保存的数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        //  判断当前集合不为空
        if (!CollectionUtils.isEmpty(attrValueList)){
            //  循环遍历
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //  赋值平台属性Id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        //  select * from base_attr_value where  attr_id=attrId;
        return baseAttrValueMapper.selectList(new QueryWrapper<BaseAttrValue>().eq("attr_id",attrId));
    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //  attrId = baseAtrrInfo.id;
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        //  平台属性值集合数据
        if (baseAttrInfo!=null){
            baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    @Override
    public IPage getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        //  springmvc 对象传值方式！
        //  设置查询条件： http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        //  mysql 默认排序方式：asc
        spuInfoQueryWrapper.orderByDesc("id");
        return spuInfoMapper.selectPage(pageParam,spuInfoQueryWrapper);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
            spuInfo
            spuImage
            spuSaleAttr
            spuSaleAttrValue
         */
        spuInfoMapper.insert(spuInfo);

        // 获取前台传递过来的spuImage列表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)){
            // 循环遍历插入数据  spuImage 只能获取到名称，url
            for (SpuImage spuImage : spuImageList) {
                // spuImage -- Id 是自增长的！
                //  spuImage -- spuId = spuInfo.getId();
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }

        //  获取spuSaleAttr
        //  销售属性：销售属性值  1对多
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)){
            //  循环遍历
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                //  一样的道理，查看数据库表中的数据，在前端是否给全！
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                //  在这个地方获取spuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    //  循环遍历
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        // 观察数据
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        // sale_attr_name 销售属性名称
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        // select * from spu_image where spu_id = spuId
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id",spuId));
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        //  根据spuId 查询销售属性集合数据   spu_sale_attr   spu_sale_attr_value
        //  select * from spu_sale_attr where spu_id = spuId;
        //        QueryWrapper<SpuSaleAttr> spuSaleAttrQueryWrapper = new QueryWrapper<>();
        //        spuSaleAttrQueryWrapper.eq("spu_id",spuId);
        //        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(spuSaleAttrQueryWrapper);
        //  select * from spu_sale_attr_value where base_sale_attr_id = spu_sale_attr.base_sale_attr_id and spu_sale_attr_value.spu_id = spu_sale_attr.spu_id;
        //  可以使用多表关联查询
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
        skuInfo;
        skuImage;
        skuSaleAttrValue; 记录sku 与 销售属性值的关系
        skuAttrValue; 记录sku 与 平台属性值的关系
         */
        skuInfoMapper.insert(skuInfo);
        //  skuImage
        //  先获取图片集合
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            // 循环遍历
            for (SkuImage skuImage : skuImageList) {
                //  赋值skuId sku_image.sku_id = sku_info.id
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }
        //  skuSaleAttrValue
        //  先获取数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            //  循环遍历
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //  赋值skuId sku_sale_attr_value.sku_id = sku_info.id
                //  sku_sale_attr_value.spu_id 也需要赋值 sku_info.spu_id
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

        //  skuAttrValue
        //  先获取数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            // 循环遍历
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                //  赋值skuId sku_attr_value.sku_id = sku_info.id
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }
    }

    @Override
    public IPage<SkuInfo> getSkuInfoList(Page<SkuInfo> skuInfoPage) {
        //  设置一个排序规则
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(skuInfoPage,skuInfoQueryWrapper);
    }

    @Override
    public void onSale(Long skuId) {
        // update sku_info set is_sale = 1 where id = skuId
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);

        //  发送消息 给service-list 调用商品的上架方法，
        //  发送的消息：
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_UPPER,skuId);

    }

    @Override
    public void cancelSale(Long skuId) {
        // update sku_info set is_sale = 0 where id = skuId 其他字段不会有变化！
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(0);
        skuInfo.setId(skuId);
        skuInfoMapper.updateById(skuInfo);
        //  发送消息
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_GOODS,MqConst.ROUTING_GOODS_LOWER,skuId);
    }


    @Override
    @GmallCache(prefix = "skuInfo:")
    public SkuInfo getSkuInfo(Long skuId) {
        /*
        if(true){
            // 缓存获取
        }else {
            // 获取数据库  getSkuInfoDB(skuId)
            // 放入缓存
        }
        //  如果redis 宕机了，应该如何处理? redisTemplate 就不存在了。 使用数据库进行兜底！
        100 万请求进来。
         */
        // return getSkuInfoByRedis(skuId);
        // return getSkuInfoByRedisson(skuId);

        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoByRedisson(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            //  skuKey sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            //  set key value; set(skuKey,SkuInfo的Json对象)    get key;
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //  判断缓存没有数据
            if (skuInfo==null){
                //  使用redisson 来解决了。
                //  定义一个分布锁的key = sku:skuId:lock
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //  获取到锁对象
                RLock lock = redissonClient.getLock(lockKey);
                //  尝试加锁，最多等待1秒，上锁以后1秒自动解锁
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //  用法：
                if (flag){
                    //  获取到锁，查询数据库
                    try {
                        skuInfo = getSkuInfoDB(skuId);
                        // 防止缓存穿透
                        if (skuInfo==null){
                            SkuInfo skuInfo1 = new SkuInfo();// 这个对象是有地址的，但是对象的属性值是空的
                            // 说明这个数据根本不存在，
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            // 返回
                            return skuInfo1;
                        }
                        //  当skuInfo 不为空，直接将数据放入缓存
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        //  返回数据
                        return skuInfo;
                    } finally {
                        //  解锁
                        lock.unlock();
                    }
                }else {
                    // 没有获取到锁！
                    Thread.sleep(1000);
                    //  睡醒了之后，要再次调用改方法
                    return getSkuInfo(skuId);
                }
            }else {
                return skuInfo;
            }
        } catch (InterruptedException e) {
            //  如果有异常，则通知管理员立刻进行维护。
            e.printStackTrace();
        }

        return getSkuInfoDB(skuId);
    }

    //  ctrl+alt+m 根据redis - set 命令+lua 脚本实现的分布式锁
    private SkuInfo getSkuInfoByRedis(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            //  查看缓存中是否有数据  必须要知道缓存的key 是谁?
            //  skuKey sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            //  获取key 对应的数据 这个数据存储的时候，使用的是哪种数据类型? String
            //  hset(key,field,value)  hget(key,field);
            //  hset(key,skuName,"荣耀xxx") hset(key,weight,0.66) hset(key,skuDefaultImg,"xxx")
            //  存储对象之所以使用hash 主要目的是：便于后续的修改操作！ 商品详情不会涉及到修改，那么直接存储String
            //  set key value; set(skuKey,SkuInfo的Json对象)    get key;
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            //  判断缓存没有数据
            if (skuInfo==null){
                //  说明缓存中没有数据，需要从数据库中获取数据，防止缓存击穿！
                //  定义一个分布锁的key = sku:skuId:lock
                String lockKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                //  定义一个uuid
                String uuid = UUID.randomUUID().toString();
                //  使用redis的set命令进行加锁
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                //  判断是否上锁成功
                if (flag){
                    // 表示上锁成功，查询数据库 skuId 在数据库中一定存在么? 143
                    skuInfo = getSkuInfoDB(skuId);
                    // 防止缓存穿透
                    if (skuInfo==null){
                        SkuInfo skuInfo1 = new SkuInfo();// 这个对象是有地址的，但是对象的属性值是空的
                        // 说明这个数据根本不存在，
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        // 返回
                        return skuInfo1;
                    }
                    //  当skuInfo 不为空，直接将数据放入缓存
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    //  将数据放入到了缓存，任务执行完成，此时需要将锁删除！ 使用lua 脚本
                    //  定义一个lua 脚本
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    //  声明一个对象 RedisScript
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                    redisScript.setScriptText(script);
                    redisScript.setResultType(Long.class);
                    //  执行lua 脚本
                    //  锁的key，和锁的值
                    redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);

                    return skuInfo;
                }else {
                    //  没有获取到锁的线程
                    Thread.sleep(1000);
                    //  睡醒了之后，要再次调用改方法
                    return getSkuInfo(skuId);
                }
            }else {
                //  如果skuInfo 不为空则直接返回！
                return skuInfo;
            }
        } catch (InterruptedException e) {
            //  获取到失败的原因，通知管理员： log 日志 ，接入短信接口，直接发信息给管理员。
            e.printStackTrace();
        }
        // 使用数据库进行兜底！
        return getSkuInfoDB(skuId);
    }

    //  这个方法是获取到skuInfo 和 skuImageList的集合数据
    private SkuInfo getSkuInfoDB(Long skuId) {
        //  select * from sku_info where id=skuId;
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //  select * from sku_image where sku_id = skuId;

        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        // skuInfo=null
        if (skuInfo!=null){
            skuInfo.setSkuImageList(skuImageList);
        }
        //  返回skuInfo 对象
        return skuInfo;
    }

    // 查询分类数据，后续会在这个方法上添加一个注解：
    /*
    多了一点功能仅此而已：
        1.  判断缓存中是否有数据，
        2.  缓存没有，才会查询数据 防止缓存击穿+防止缓存穿透的功能
        3.  缓存有，则直接查询缓存
     */
    @Override
    //    @Transactional(value = "atguigu")
    @GmallCache(prefix = "categoryView:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        // select  * from base_category_view where id = category3Id;
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "skuPrice:") // key = skuPrice:skuId value = price;
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo!=null){
            //  只返回当前价格数据
            return skuInfo.getPrice();
        }
        //  如果没有查询到数据，则返回默认值0
        return new BigDecimal(0);
    }

    @Override
    @GmallCache(prefix = "spuSaleAttrListCheck:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        // 返回数据
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }


    @Override
    @GmallCache(prefix = "skuIdsMap:")
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        //  map要存储数据 ，应该是从数据库中获取 执行sql 语句
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (!CollectionUtils.isEmpty(mapList)){
            // 循环遍历
            for (Map skuMap : mapList) {
                //  {"106|110":40,"107|110":41}
                map.put(skuMap.get("value_ids"),skuMap.get("sku_id"));
            }
        }
        return map;
    }

    @Override
    @GmallCache(prefix = "index:")
    public List<JSONObject> getBaseCategoryList() {
        //  声明一个集合
        List<JSONObject> jsonObjectList = new ArrayList<>();
        /*
        1.  获取到所有的分类名称+分类Id
        2.  构建Json 格式的数据
         */
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //  以一级分类Id 进行分组 Collectors.groupingBy(BaseCategoryView::getCategory1Id)
        //  key = category1Id value = List<BaseCategoryView>
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //  声明一个index
        int index = 1;
        //  循环遍历当前的集合category1Map
        for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
            //  获取一级分类Id
            Long category1Id = entry.getKey();
            //  获取到一级分类Id 下集合数据 {包含分类名称，分类Id}
            List<BaseCategoryView> categoryViewList1 = entry.getValue();
            //  创建一个对象
            JSONObject category1 = new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",category1Id);
            //  当以分类Id进行分组之后，后面的所有分类名称都一样！
            category1.put("categoryName",categoryViewList1.get(0).getCategory1Name());
            //  category1.put("categoryChild","暂无");
            //  变量的更新
            index++;

            //  获取二级分类数据：以二级分类Id 进行分组获取数据
            Map<Long, List<BaseCategoryView>> category2Map = categoryViewList1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //  声明一个集合来存储二级分类的数据
            List<JSONObject> category2Child = new ArrayList<>();
            //  循环变量
            for (Map.Entry<Long, List<BaseCategoryView>> entry1 : category2Map.entrySet()) {
                //  获取二级分类Id
                Long category2Id = entry1.getKey();

                // 获取二级分类Id 下的所有数据
                List<BaseCategoryView> categoryViewList2 = entry1.getValue();

                //  声明对象
                JSONObject category2 = new JSONObject();
                category2.put("categoryId",category2Id);
                category2.put("categoryName",categoryViewList2.get(0).getCategory2Name());

                //  将二级分类对象添加到集合中
                category2Child.add(category2);

                //  声明一个集合来存储三级分类的数据
                List<JSONObject> category3Child = new ArrayList<>();
                //  获取三级分类数据
                // Consumer void accept(T t)
                categoryViewList2.stream().forEach((category3View)->{
                    //  声明三级分类对象
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId",category3View.getCategory3Id());
                    category3.put("categoryName",category3View.getCategory3Name());
                    //  需要将三级分类对象添加到集合
                    category3Child.add(category3);
                });
                //  将三级分类集合放入二级
                category2.put("categoryChild",category3Child);
            }
            //  将二级分类集合放入一级
            category1.put("categoryChild",category2Child);

            //  需要将所有的一级分类数据添加到集合
            jsonObjectList.add(category1);
        }
        //  返回集合
        return jsonObjectList;
    }

    @Override
    public BaseTrademark getBaseTrademarkById(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        //  有可能会进行多表关联查询：base_attr_info , base_attr_value , sku_attr_value
        //  展示的时候：展示的是平台属性名称，平台属性值的名称
        return baseAttrInfoMapper.selectAttrListBySkuId(skuId);
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        //  模糊查询：
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.like("sku_name",keyword);
        return skuInfoMapper.selectList(skuInfoQueryWrapper);
    }

    @Override
    public List<SkuInfo> findSkuInfoBySkuIdList(List<Long> skuIdList) {
        return skuInfoMapper.selectBatchIds(skuIdList);
    }

    @Override
    public List<SpuInfo> findSpuInfoByKeyword(String keyword) {
        //  根据名称进行查询
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.like("spu_name",keyword);
        return spuInfoMapper.selectList(spuInfoQueryWrapper);
    }

    @Override
    public List<SpuInfo> findSpuInfoBySpuIdList(List<Long> spuIdList) {
        return spuInfoMapper.selectBatchIds(spuIdList);
    }

    @Override
    public List<BaseCategory3> findBaseCategory3ByCategory3IdList(List<Long> category3IdList) {
        return baseCategory3Mapper.selectBatchIds(category3IdList);
    }

}
