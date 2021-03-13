package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.activity.service.ActivityService;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.model.activity.ActivityRule;
import com.atguigu.gmall.model.activity.CouponInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-13 11:38:03
 */
@Service
public class ActivityServiceImp implements ActivityService {

    @Autowired
    private ActivityInfoService activityInfoService;

    @Autowired
    private CouponInfoService couponInfoService;


    @Override
    public Map<String, Object> findActivityAndCoupon(Long skuId, Long userId) {
        Map<String, Object> map = new HashMap<>();
        /*
         1.  获取促销活动的： findActivityRule(Long skuId)
         2.  获取优惠券： findCouponInfo(Long skuId, Long activityId, Long userId)
                activityId 如何获取? findActivityRule(Long skuId) 的返回值获取到！
         */
        List<ActivityRule> activityRuleList = activityInfoService.findActivityRule(skuId);

        //  获取到activityId
        long activityId = 0;
        //  判断不为空
        if (!CollectionUtils.isEmpty(activityRuleList)){
            activityId=activityRuleList.get(0).getActivityId();
        }
        List<CouponInfo> couponInfoList = couponInfoService.findCouponInfo(skuId, activityId, userId);
        //  存储促销活动规则
        map.put("activityRuleList",activityRuleList);
        map.put("couponInfoList",couponInfoList);
        //  返回数据
        return map;
    }
}
