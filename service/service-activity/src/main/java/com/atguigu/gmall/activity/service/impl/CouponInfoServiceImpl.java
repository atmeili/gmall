package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.CouponInfoMapper;
import com.atguigu.gmall.activity.mapper.CouponRangeMapper;
import com.atguigu.gmall.activity.mapper.CouponUseMapper;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRange;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.atguigu.gmall.model.activity.CouponUse;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.CouponRangeType;
import com.atguigu.gmall.model.enums.CouponStatus;
import com.atguigu.gmall.model.enums.CouponType;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mqx
 * @date 2021-3-12 11:43:16
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Autowired
    private CouponUseMapper couponUseMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public IPage getCouponInfoList(Page<CouponInfo> couponInfoPage) {

        //  构造排序条件
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.orderByDesc("id");
        IPage<CouponInfo> couponInfoIPage = couponInfoMapper.selectPage(couponInfoPage, couponInfoQueryWrapper);

        couponInfoIPage.getRecords().stream().forEach(couponInfo -> {
            //  赋值优惠券的类型
            couponInfo.setCouponTypeString(CouponType.getNameByType(couponInfo.getCouponType()));
            //  赋值优惠券范围
            if (couponInfo!=null){
                couponInfo.setRangeTypeString(CouponRangeType.getNameByType(couponInfo.getRangeType()));
            }
        });

        //  返回数据集合
        return couponInfoIPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        /*
        优惠券couponInfo 与 couponRange 要一起操作：先删除couponRange ，更新couponInfo ，再新增couponRange ！
         */
        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",couponRuleVo.getCouponId());
        couponRangeMapper.delete(couponRangeQueryWrapper);

        //  更新数据
        CouponInfo couponInfo = this.getById(couponRuleVo.getCouponId());
        // couponInfo.setCouponType();
        couponInfo.setRangeType(couponRuleVo.getRangeType().name());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setConditionNum(couponRuleVo.getConditionNum());
        couponInfo.setBenefitDiscount(couponRuleVo.getBenefitDiscount());
        couponInfo.setBenefitAmount(couponRuleVo.getBenefitAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());

        couponInfoMapper.updateById(couponInfo);

        //  插入优惠券的规则 couponRangeList
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            //  coupon_range.coupon_id=coupon_info.id
            couponRange.setCouponId(couponRuleVo.getCouponId());
            //  插入数据
            couponRangeMapper.insert(couponRange);
        }
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        //  声明map 集合存储数据
        Map<String, Object> map = new HashMap<>();
        //  通过id 查询 coupon_range
        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",id);
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(couponRangeQueryWrapper);

        //  细节处理： 根据范围选择获取到不同的数据进行存储！
        //  coupon_range.range_id 获取到这个Id{range_id} 即可！
        List<Long> rangeIdList = couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        //  coupon_range ， 单独查询coupon_info
        CouponInfo couponInfo = this.getById(id);
        //  判断
        if(!CollectionUtils.isEmpty(rangeIdList)){
            //  遍历： 应该获取对应的类型！判断
            if ("SPU".equals(couponInfo.getRangeType())){
                //  根据rangeIdList 获取对应的spuInfoList
               List<SpuInfo> spuInfoList =  productFeignClient.findSpuInfoBySpuIdList(rangeIdList);
                map.put("spuInfoList",spuInfoList);
            } else if("TRADEMARK".equals(couponInfo.getRangeType())){
                List<BaseTrademark> trademarkList = productFeignClient.findBaseTrademarkByTrademarkIdList(rangeIdList);
                map.put("trademarkList",trademarkList);
            }else {
                List<BaseCategory3> category3List = productFeignClient.findBaseCategory3ByCategory3IdList(rangeIdList);
                map.put("category3List",category3List);
            }
            //            for (CouponRange couponRange : couponRangeList) {
            //                if ("SPU".equals(couponRange.getRangeType())){
            //                    //  根据rangeIdList 获取对应的spuInfoList
            //                   List<SpuInfo> spuInfoList =  productFeignClient.findSpuInfoBySpuIdList(rangeIdList);
            //                    map.put("spuInfoList",spuInfoList);
            //                } else if("TRADEMARK".equals(couponRange.getRangeType())){
            //                    List<BaseTrademark> trademarkList = productFeignClient.findBaseTrademarkByTrademarkIdList(rangeIdList);
            //                    map.put("trademarkList",trademarkList);
            //                }else {
            //                    List<BaseCategory3> category3List = productFeignClient.findBaseCategory3ByCategory3IdList(rangeIdList);
            //                    map.put("category3List",category3List);
            //                }
            //            }
        }
        //  返回数据
        return map;
    }

    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {
        //  模糊查询
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.like("coupon_name",keyword);
        return couponInfoMapper.selectList(couponInfoQueryWrapper);
    }

    @Override
    public List<CouponInfo> findCouponInfo(Long skuId, Long activityId, Long userId) {
        //  获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo==null) return new ArrayList<>();

        //  获取普通优惠券：与活动没有关系！ null 或者 0 普通优惠券！
        List<CouponInfo> couponInfoList = couponInfoMapper.selectCouponInfoList(skuInfo.getSpuId(), skuInfo.getCategory3Id(), skuInfo.getTmId(), userId);
        //  activityId 判断这个优惠券与活动的关系！
        if (activityId!=null){
            //  有活动,获取优惠券列表！ 范围： spu,三级分类Id,品牌
            List<CouponInfo> activityCouponInfoList = couponInfoMapper.selectActivityCouponInfoList(skuInfo.getSpuId(), skuInfo.getCategory3Id(), skuInfo.getTmId(), activityId, userId);
            //  做一个合并
            couponInfoList.addAll(activityCouponInfoList);
        }

        return couponInfoList;
    }

    //  提交数据的时候： 我们走的页面 2.00
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void getCouponInfo(Long userId, Long couponId) {
        // 判断当前优惠券是否已经领完
        CouponInfo couponInfo = this.getById(couponId);
        //  判断是否还有剩余 10
        if (couponInfo.getTakenCount()>=couponInfo.getLimitNum()){
            throw new GmallException(ResultCodeEnum.COUPON_LIMIT_GET);
        }
        // 判断这个用户是否已经领用过！
        QueryWrapper<CouponUse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id",couponId);
        queryWrapper.eq("user_id",userId);
        Integer count = couponUseMapper.selectCount(queryWrapper);
        if (count>0) {
            throw new GmallException(ResultCodeEnum.COUPON_GET);
        }
        //        CouponInfo couponInfoQuery = couponInfoMapper.selectOne(couponInfoQueryWrapper);
        //        if (couponInfoQuery!=null){
        //            throw new GmallException(ResultCodeEnum.COUPON_GET);
        //        }

        //  更新数据
        int takenCout = couponInfo.getTakenCount()+1;
        couponInfo.setTakenCount(takenCout);
        //  更新数据
        couponInfoMapper.updateById(couponInfo);

        //  插入数据到coupon_use
        CouponUse couponUse = new CouponUse();
        couponUse.setCouponId(couponId);
        couponUse.setUserId(userId);
        couponUse.setCouponStatus(CouponStatus.NOT_USED.name());
        couponUse.setGetTime(new Date());
        couponUse.setExpireTime(couponInfo.getExpireTime());

        couponUseMapper.insert(couponUse);
    }

    @Override
    public IPage<CouponInfo> getPageByUserId(Page<CouponInfo> couponInfoPage, Long userId) {
        //  coupon_info,coupon_use
        return couponInfoMapper.selectPageByUserId(couponInfoPage,userId);
    }

    /**
     * map key = skuId  value = List<CouponInfo>
     * @param cartInfoList 购物车列表
     * @param skuIdToActivityIdMap  判断当前skuId 是否参与了活动
     * @param userId 判断是否已经领用！
     * @return
     */
    @Override
    public Map<Long, List<CouponInfo>> findCartCouponInfo(List<CartInfo> cartInfoList, Map<Long, Long> skuIdToActivityIdMap, Long userId) {
        //  记录优惠券使用范围 spuId,tmId,category3Id 暂时存在到 map 中！
        //  map key=  value= "range:1:" + skuInfo.getSpuId() tmId category3Id  value = skuIdList
        Map<String, List<Long>> rangeToSkuIdMap = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            //  获取到对应的skuInfo
            SkuInfo skuInfo = productFeignClient.getSkuInfo(cartInfo.getSkuId());
            //  skuInfo 获取范围类型用的。
            //  skuInfo 包含spuId,tmId,category3Id;
            this.setRuleData(skuInfo,rangeToSkuIdMap);
        }

        /**
         * rangeType(范围类型)  1:商品(spuId) 2:品类(category3Id) 3:品牌tmId
         * rangeId(范围id) spuId, categoryId , tmId,
         * 同一张优惠券不能包含多个范围类型，同一张优惠券可以对应同一范围类型的多个范围id（即：同一张优惠券可以包含多个spuId）
         * 示例数据：
         * couponId   rangeType   rangeId
         * 1             1             20
         * 1             1             30
         * 2             2             20
         */
        // 通过skuId 获取到skuInfo ,skuInfo 中  spuId, categoryId , tmId 都存在！
        //  声明一个集合来存储skuInfo
        List<SkuInfo> skuInfoList = new ArrayList<>();
        //  循环赋值
        for (CartInfo cartInfo : cartInfoList) {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(cartInfo.getSkuId());
            skuInfoList.add(skuInfo);
        }
        if (CollectionUtils.isEmpty(skuInfoList)) return new HashMap<>();

        //  查询优惠券列表我们需要优惠券使用范围类型rangeType  rangeId  userId
        //  这个优惠券中有rangeId 通过这个方法，可以给这个字段进行赋值coupon_range.range_id
        List<CouponInfo> allCouponInfoList = couponInfoMapper.selectCartCouponInfoList(skuInfoList,userId);

        //  循环遍历所有的优惠券集合列表
        for (CouponInfo couponInfo : allCouponInfoList) {
            //  获取到对应的优惠券类型
            String rangeType = couponInfo.getRangeType();
            //  获取到对应的range_id
            Long rangeId = couponInfo.getRangeId();
            //  目的：key = skuId value = List<CouponInfo>
            //  如何知道这个skuId 是否参与了活动！ skuIdToActivityIdMap key = skuId value = activityId

            //  优惠券：活动优惠券{activityId 不为空} debug  + 普通优惠券
            if (couponInfo.getActivityId()!=null){
                //  声明一个skuIdList 集合
                List<Long> skuIdList = new ArrayList<>();
                //  skuIdToActivityIdMap 中存储的数据 参加活动的skuId
                Iterator<Map.Entry<Long, Long>> iterator = skuIdToActivityIdMap.entrySet().iterator();
                //  循环遍历当前的集合
                while (iterator.hasNext()){
                    Map.Entry<Long, Long> entry = iterator.next();
                    Long skuId = entry.getKey();
                    Long activityId = entry.getValue();

                    //  判断你的活动Id 下 有哪些skuId 对应的优惠券 说明是同一个活动！
                    if (couponInfo.getActivityId().intValue()==activityId.intValue()){
                        //  找到skuId了,找到优惠券！
                        //  优惠券：coupon_range.range_type coupon_range.range_id
                        //  判断优惠券的类型： spuId,category3Id,tmId
                        //  通过skuId 获取到skuInfo
                        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                        //  判断优惠券的范围
                        if (couponInfo.getRangeType().equals(CouponRangeType.SPU.name())){
                            //  判断rageId
                            if (couponInfo.getRangeId().intValue()==skuInfo.getSpuId().intValue()){
                                //  记录这个skuId 属于哪个范围的优惠券！
                                skuIdList.add(skuId);
                            }
                        }else if (couponInfo.getRangeType().equals(CouponRangeType.TRADEMARK.name())){
                            if (couponInfo.getRangeId().intValue()==skuInfo.getTmId().intValue()){
                                //  记录这个skuId 属于哪个范围的优惠券！
                                skuIdList.add(skuId);
                            }
                        }else {
                            if (couponInfo.getRangeId().intValue()==skuInfo.getCategory3Id().intValue()){
                                //  记录这个skuId 属于哪个范围的优惠券！
                                skuIdList.add(skuId);
                            }
                        }
                    }
                }
                //  属于活动优惠券的！ 将这个skuIdList 集合 赋值给优惠券
                couponInfo.setSkuIdList(skuIdList);
            }else {
                //  普通优惠券
                //  判断使用范围 spuId
                //  一个skuId 对应的优惠券使用范围：
                if (rangeType.equals(CouponRangeType.SPU.name())){
                    //  setRuleData 初始化优惠券使用范围的规则  rangeToSkuIdMap key=spuId,tmId,category3Id ,value=skuIdList
                    //  "range:1:" + skuInfo.getSpuId()  因为 rangeId 这个字段对应的存储 ： spuId,tmId,category3Id
                    couponInfo.setSkuIdList(rangeToSkuIdMap.get("range:1:" + rangeId));
                }else if (rangeType.equals(CouponRangeType.TRADEMARK.name())){
                    //  判断使用范围 tmId
                    couponInfo.setSkuIdList(rangeToSkuIdMap.get("range:2:" + rangeId));
                }else {
                    //  category3Id
                    couponInfo.setSkuIdList(rangeToSkuIdMap.get("range:3:" + rangeId));
                }
            }
        }
        //  给优惠券赋值：  优惠券对应的skuId列表
        //  目的： map key = skuId  value = List<CouponInfo>
        Map<Long, List<CouponInfo>> skuIdToCouponInfoListMap = new HashMap<>();

        //  循环遍历当前的所有优惠券集合
        for (CouponInfo couponInfo : allCouponInfoList) {
            //  获取到优惠券下对应的skuId集合
            List<Long> skuIdList = couponInfo.getSkuIdList();
            //  遍历当前skuIdList
            for (Long skuId : skuIdList) {
                //  使用skuIdToCouponInfoListMap 这个集合判断
                //  这个集合有对应的skuId 时， key = skuId 这个skuId 对应了多个优惠券
                if (skuIdToCouponInfoListMap.containsKey(skuId)){
                    //  从原有集合中获取到数据 并放入map
                    List<CouponInfo> couponInfoList = skuIdToCouponInfoListMap.get(skuId);
                    couponInfoList.add(couponInfo);
                }else {
                    //  第一次进来就走这！
                    List<CouponInfo> couponInfoList = new ArrayList<>();
                    couponInfoList.add(couponInfo);
                    //  没有skuId 时
                    skuIdToCouponInfoListMap.put(skuId,couponInfoList);
                }
            }
        }
        return skuIdToCouponInfoListMap;
    }

    //  设置优惠券对应的存储规则！ 做个初始化操作。
    private void setRuleData(SkuInfo skuInfo, Map<String, List<Long>> rangeToSkuIdMap) {
        //  优惠券对应的使用范围key spuId
        //  rangeToSkuIdMap  key = key1  value = skuIdList
        String key1 = "range:1:" + skuInfo.getSpuId(); // 1,2,3,4,5  add(6);
        if (rangeToSkuIdMap.containsKey(key1)){
            //  获取对应的数据
            List<Long> skuIdList = rangeToSkuIdMap.get(key1);
            //  将对应的skuId skuInfo.getId();
            skuIdList.add(skuInfo.getId());
        }else {
            //  说明没有这个key，声明一个集合将skuId 添加进去，并保存到map 集合中！  skuId=40
            List<Long> skuIdList = new ArrayList<>();
            skuIdList.add(skuInfo.getId());
            rangeToSkuIdMap.put(key1,skuIdList);
        }
        //  范围 category3Id
        String key2 = "range:2:" + skuInfo.getCategory3Id();  // skuId = 40
        if (rangeToSkuIdMap.containsKey(key2)){
            //  获取对应的数据
            List<Long> skuIdList = rangeToSkuIdMap.get(key2);
            //  将对应的skuId skuInfo.getId();
            skuIdList.add(skuInfo.getId());

        }else {
            //  说明没有这个key，声明一个集合将skuId 添加进去，并保存到map 集合中！
            List<Long> skuIdList = new ArrayList<>();
            skuIdList.add(skuInfo.getId());
            rangeToSkuIdMap.put(key2,skuIdList);
        }
        //  范围 tmId
        String key3 = "range:3:" + skuInfo.getTmId();   // skuId = 40;
        if(rangeToSkuIdMap.containsKey(key3)) {
            List<Long> skuIdList = rangeToSkuIdMap.get(key3);
            skuIdList.add(skuInfo.getId());
        } else {
            List<Long> skuIdList = new ArrayList<>();
            skuIdList.add(skuInfo.getId());
            rangeToSkuIdMap.put(key3, skuIdList);
        }

    }
}
