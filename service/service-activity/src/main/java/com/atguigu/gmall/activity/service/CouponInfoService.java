package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-12 11:42:51
 */
public interface CouponInfoService extends IService<CouponInfo> {


    //  带分页的数据查询
    IPage getCouponInfoList(Page<CouponInfo> couponInfoPage);

    /**
     * 大保存
     * @param couponRuleVo
     */
    void saveCouponRule(CouponRuleVo couponRuleVo);

    /**
     * 优惠券范围
     * @param id
     * @return
     */
    Map<String, Object> findCouponRuleList(Long id);

    /**
     *
     * @param keyword
     * @return
     */
    List<CouponInfo> findCouponByKeyword(String keyword);

    /**
     * 获取优惠券信息
     * @param skuId 获取skuInfo
     * @param activityId 判断优惠券与活动是否有关系
     * @param userId 判断这个用户是否领用
     * @return
     */
    List<CouponInfo> findCouponInfo(Long skuId, Long activityId, Long userId);

    /**
     * 领取优惠券
     * @param userId
     * @param couponId
     */
    void getCouponInfo(Long userId, Long couponId);

    /**
     * 根据用户Id 查询优惠券列表
     * @param couponInfoPage
     * @param userId
     * @return
     */
    IPage<CouponInfo> getPageByUserId(Page<CouponInfo> couponInfoPage, Long userId);

    /**
     * 获取优惠券列表
     * @param cartInfoList
     * @param skuIdToActivityIdMap
     * @param userId
     * @return
     */
    Map<Long, List<CouponInfo>> findCartCouponInfo(List<CartInfo> cartInfoList, Map<Long, Long> skuIdToActivityIdMap, Long userId);
}
