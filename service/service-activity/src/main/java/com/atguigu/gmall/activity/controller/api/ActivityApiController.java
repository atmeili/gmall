package com.atguigu.gmall.activity.controller.api;

import com.atguigu.gmall.activity.service.ActivityService;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.cart.CarInfoVo;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-13 11:26:59
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityApiController {

    //  ActivityService 主要起的汇总数据 优惠券+ 促销活动
    @Autowired
    private ActivityService activityService;

    @Autowired
    private CouponInfoService couponInfoService;

    @GetMapping("findActivityAndCoupon/{skuId}")
    public Result  findActivityAndCoupon(@PathVariable Long skuId, HttpServletRequest request){
        //  获取到用户Id
        String userId = AuthContextHolder.getUserId(request);
        //  判断是否为空
        if (StringUtils.isEmpty(userId)){
            userId="0";
        }

        /*
        1.  获取促销活动的： findActivityRule(Long skuId)
        2.  获取优惠券： findCouponInfo(Long skuId, Long activityId, Long userId)
                activityId 如何获取? findActivityRule(Long skuId) 的返回值获取到！
         */
        Map<String ,Object> map = activityService.findActivityAndCoupon(skuId,Long.parseLong(userId));
        //  返回数据
        return Result.ok(map);
    }
    //  重点看URL
    //  http://api.gmall.com/api/activity/auth/getCouponInfo/8
    @GetMapping(value = "auth/getCouponInfo/{couponId}")
    public Result getCouponInfo(@PathVariable Long couponId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        //  调用服务层方法
        couponInfoService.getCouponInfo(Long.parseLong(userId),couponId);

        return Result.ok();
    }

    //  查询优惠券列表!
    @GetMapping("auth/{page}/{limit}")
    public Result couponList(@PathVariable Long page,
                             @PathVariable Long limit,
                             HttpServletRequest request){

        String userId = AuthContextHolder.getUserId(request);
        //  构建Page
        Page<CouponInfo> couponInfoPage = new Page<>(page,limit);
        //  调用服务层方法
        IPage<CouponInfo> couponInfoIPage = couponInfoService.getPageByUserId(couponInfoPage,Long.parseLong(userId));
        //  返回数据
        return Result.ok(couponInfoIPage);
    }

    //  汇总数据 促销活动列表 + 优惠券列表
    @PostMapping("inner/findCartActivityAndCoupon/{userId}")
    public List<CarInfoVo> findCartActivityAndCoupon(@PathVariable Long userId,@RequestBody List<CartInfo> cartInfoList){
        //  调用服务层方法
        return activityService.findCartActivityAndCoupon(cartInfoList,userId);
    }
}
