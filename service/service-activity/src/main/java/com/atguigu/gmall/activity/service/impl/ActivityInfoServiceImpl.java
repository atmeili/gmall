package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.ActivityInfoMapper;
import com.atguigu.gmall.activity.mapper.ActivityRuleMapper;
import com.atguigu.gmall.activity.mapper.ActivitySkuMapper;
import com.atguigu.gmall.activity.mapper.CouponInfoMapper;
import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.model.activity.*;
import com.atguigu.gmall.model.cart.CarInfoVo;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.ActivityType;
import com.atguigu.gmall.model.product.SkuInfo;
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
 * @date 2021-3-9 14:35:39
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    //  实现类中通常会引入mapper
    @Autowired
    private ActivityInfoMapper activityInfoMapper;

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Autowired
    private CouponInfoService couponInfoService;

    @Override
    public IPage<ActivityInfo> getPage(Page<ActivityInfo> infoPage) {

        //  构造查询条件
        QueryWrapper<ActivityInfo> activityInfoQueryWrapper = new QueryWrapper<>();
        activityInfoQueryWrapper.orderByDesc("id");
        //  mysql 默认的排序规则？ asc ,desc
        IPage<ActivityInfo> activityInfoIPage = activityInfoMapper.selectPage(infoPage, activityInfoQueryWrapper);
        //  细节： 活动的数据类型： 在表中不存在！activityTypeString
        //  Consumer  void accept(T t);
        activityInfoIPage.getRecords().stream().forEach((activityInfo)->{
            //  如何获取到对应的数据ActivityType.getNameByType(type) ;
            activityInfo.setActivityTypeString(ActivityType.getNameByType(activityInfo.getActivityType()));
        });

        //  返回数据！
        return activityInfoIPage;
    }

    //  ActivityRuleVo ： 既包含保存，同时也可以包含修改内容！
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        //  在写平台属性保存的时候： 先删除对应的数据，然后再新增数据！
        //  原来对应的商品活动范围列表删除：
        activitySkuMapper.delete(new QueryWrapper<ActivitySku>().eq("activity_id",activityRuleVo.getActivityId()));
        //  删除原来的活动规则列表
        activityRuleMapper.delete(new QueryWrapper<ActivityRule>().eq("activity_id",activityRuleVo.getActivityId()));
        //  优惠券列表 暂时先不写！
        //  获取到优惠券列表的Id集合
        List<Long> couponIdList = activityRuleVo.getCouponIdList();
        //  coupon_info.activity_id = 赋值！
        //  先接触绑定！
        //  接触绑定的时候：couponIdList 是空的！ 传递数据的时候一定会有一个活动Id
        //  根据活动Id 进行更新  update coupon_info set activity_id = 0 where activity_id = activityRuleVo.getActivityId();
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        CouponInfo couponInfo = new CouponInfo();
        couponInfo.setActivityId(0L);
        couponInfoQueryWrapper.eq("activity_id",activityRuleVo.getActivityId());
        couponInfoMapper.update(couponInfo,couponInfoQueryWrapper);

        //  保存数据：
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();

        for (ActivitySku activitySku : activitySkuList) {
            //  需要将活动Id 赋值给当前对象
            activitySku.setActivityId(activityRuleVo.getActivityId());
            activitySkuMapper.insert(activitySku);
        }

        for (ActivityRule activityRule : activityRuleList) {
            //  需要将活动Id 赋值给当前对象
            activityRule.setActivityId(activityRuleVo.getActivityId());
            activityRuleMapper.insert(activityRule);
        }

        //  如果这个集合不为空！
        if(!CollectionUtils.isEmpty(couponIdList)){
            //  循环
            for (Long couponId : couponIdList) {
                CouponInfo couponInfoUp = new CouponInfo();
                couponInfoUp.setId(couponId);
//                CouponInfo couponInfoUp = couponInfoService.getById(couponId);
//                CouponInfo couponInfoUp = couponInfoMapper.selectById(couponId);
                couponInfoUp.setActivityId(activityRuleVo.getActivityId());
                couponInfoMapper.updateById(couponInfoUp);
            }
            //  更新！
        }
    }

    @Override
    public List<SkuInfo> findSkuInfoByKeyword(String keyword) {
        /*
          1.    获取到所有的商品列表
          2.    将skuInfoList 集合列表中的skuId 获取出来，同时变成一个集合
          3.    找到哪些商品skuId 正在参加活动！ skuIdList
         */
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoByKeyword(keyword);
        //  将skuInfoList 集合列表中的skuId 获取出来，同时变成一个集合
        //  skuIdList {1，2，3，4，5，6}
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfo::getId).collect(Collectors.toList());
        // 找到哪些商品skuId 正在参加活动！
        //  existSkuIdList {3，4}
        List<Long> existSkuIdList = activityInfoMapper.selectExistSkuIdList(skuIdList);

        //  通过这个existSkuIdList 中的skuId找到skuInfo
        List<SkuInfo> skuInfos = existSkuIdList.stream().map(skuId -> {
            return productFeignClient.getSkuInfo(skuId);
        }).collect(Collectors.toList());
        //        List<SkuInfo> skuInfos = new ArrayList<>();
        //        for (Long skuId : existSkuIdList) {
        //            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        //            skuInfos.add(skuInfo);
        //        }
        //  去重 removeAll 使用equals 方法进行判断的！ 重写equals 方法！
        skuInfoList.removeAll(skuInfos);

        return skuInfoList;
    }

    @Override
    public Map<String, Object> findActivityRuleList(Long activityId) {
        Map<String, Object> map = new HashMap<>();
        //  获取活动规则列表 activity_rule
        QueryWrapper<ActivityRule> activityRuleQueryWrapper = new QueryWrapper<>();
        activityRuleQueryWrapper.eq("activity_id",activityId);
        List<ActivityRule> activityRuleList = activityRuleMapper.selectList(activityRuleQueryWrapper);

        map.put("activityRuleList",activityRuleList);

        //  存储活动范围列表对应的skuInfo activity_sku
        //  先获取活动Id 对应的skuId
        QueryWrapper<ActivitySku> activitySkuQueryWrapper = new QueryWrapper<>();
        activitySkuQueryWrapper.eq("activity_id",activityId);
        List<ActivitySku> activitySkuList = activitySkuMapper.selectList(activitySkuQueryWrapper);

        //  将对应的skuId 转换成一个集合
        List<Long> skuIdList = activitySkuList.stream().map(ActivitySku::getSkuId).collect(Collectors.toList());

        //  通过这个集合获取skuInfo 数据即可！
        List<SkuInfo> skuInfoList = productFeignClient.findSkuInfoBySkuIdList(skuIdList);

        map.put("skuInfoList",skuInfoList);

        //  回显绑定的优惠券！
        //  优惠券在这个表中 coupon_info
        List<CouponInfo> couponInfoList = couponInfoMapper.selectList(new QueryWrapper<CouponInfo>().eq("activity_id", activityId));
        map.put("couponInfoList",couponInfoList);
        return map;
    }

    @Override
    public List<ActivityRule> findActivityRule(Long skuId) {
        //  声明集合对象
        //  activity_rule，activity_sku，activity_info
        List<ActivityRule> activityRuleList = activityInfoMapper.selectActivityRuleList(skuId);
        //  返回数据
        return activityRuleList;
    }

    //  获取促销活动列表
    @Override
    public List<CarInfoVo> findCartActivityRuleMap(List<CartInfo> cartInfoList, Map<Long, Long> skuIdToActivityIdMap) {
        /*
            已知 cartInfoList； 活动Id 下有哪些skuId ，通过这个skuId 能够找到CartInfo
            cartInfoList 下面有多少个skuId
         */
        List<CarInfoVo> carInfoVoList = new ArrayList<>();

        //  定义map 集合 key=skuId value = cartInfo;
        Map<Long, CartInfo> skuIdToCartInfoMap = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            skuIdToCartInfoMap.put(cartInfo.getSkuId(),cartInfo);
        }
        //  获取到skuId 集合列表
        List<Long> skuIdList = cartInfoList.stream().map(CartInfo::getSkuId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(skuIdList)) return new ArrayList<>();

        //  找到对应的活动规则 : 这个方法给skuId 进行赋值！
        List<ActivityRule> activityRuleList = activityInfoMapper.selectCartActivityRuleList(skuIdList);

        //  以skuId 进行分组 key = skuId value = List<ActivityRule>
        Map<Long, List<ActivityRule>> skuIdToActivityRuleListMap = activityRuleList.stream().collect(Collectors.groupingBy(activityRule -> activityRule.getSkuId()));


        //  ActivityRule 这个实体类中封装过了一个 skuId
        //  以activityId 进行分组
        //  key = activityId value = List<ActivityRule>
        Map<Long, List<ActivityRule>> activityIdToActivityRuleListAllMap = activityRuleList.stream().collect(Collectors.groupingBy(activityRule -> activityRule.getActivityId()));

        //  循环遍历获取数据
        Iterator<Map.Entry<Long, List<ActivityRule>>> iterator = activityIdToActivityRuleListAllMap.entrySet().iterator();
        while (iterator.hasNext()){
            //  获取数据
            Map.Entry<Long, List<ActivityRule>> entry = iterator.next();
            //  获取key
            Long activityId = entry.getKey();
            //  获取value
            List<ActivityRule> currentActivityRuleList = entry.getValue();

            //  活动规则下有哪些skuId? ActivityRule 这个对象中已经有skuId , 获取skuId ,活动规则对应的skuId
            //  分组的话：不能重复！ 但是返回的是map 集合，我们需要的是skuId 集合列表！
            //  40 ,41 ,42, 43
            Set<Long> activitySkuIdSet = currentActivityRuleList.stream().map(activityRule -> activityRule.getSkuId()).collect(Collectors.toSet());

            //  声明一个对象：CarInfoVo
            CarInfoVo carInfoVo = new CarInfoVo();

            List<CartInfo> cartInfos = new ArrayList<>();
            //  有了skuId 循环遍历
            for (Long skuId : activitySkuIdSet) {
                //  记录一下skuId 对应的哪个活动Id  key = skuId value = activityId
                skuIdToActivityIdMap.put(skuId,activityId);
                //  获取cartInfo
                CartInfo cartInfo = skuIdToCartInfoMap.get(skuId);
                cartInfos.add(cartInfo);
            }
            carInfoVo.setCartInfoList(cartInfos);

            //  根据skuId 获取对应的活动规则
            //  所有的skuId集合
            List<ActivityRule> ruleList = skuIdToActivityRuleListMap.get(activitySkuIdSet.iterator().next());
            carInfoVo.setActivityRuleList(ruleList);
            //  添加数据到集合
            carInfoVoList.add(carInfoVo);
        }

        return carInfoVoList;
    }


}
