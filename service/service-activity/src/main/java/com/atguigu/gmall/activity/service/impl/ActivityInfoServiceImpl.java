package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.ActivityInfoMapper;
import com.atguigu.gmall.activity.mapper.ActivityRuleMapper;
import com.atguigu.gmall.activity.mapper.ActivitySkuMapper;
import com.atguigu.gmall.activity.mapper.CouponInfoMapper;
import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.model.activity.*;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        for (Long couponId : couponIdList) {
            CouponInfo couponInfo = couponInfoService.getById(couponId);
            couponInfo.setActivityId(0L);
            //  更新 update coupon_info set activity_id = 0L where activity_id = ?
            //        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
            //        couponInfoQueryWrapper.eq("activity_id",activityRuleVo.getActivityId());
            //  couponInfoMapper.update(couponInfo,couponInfoQueryWrapper);
            couponInfoMapper.updateById(couponInfo);
        }


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
        if(CollectionUtils.isEmpty(couponIdList)){
            //  循环
            for (Long couponId : couponIdList) {
//                CouponInfo couponInfoUp = new CouponInfo();
//                couponInfoUp.setId(couponId);
                CouponInfo couponInfoUp = couponInfoService.getById(couponId);
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
        return map;
    }

}
