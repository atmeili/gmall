package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.cart.CarInfoVo;
import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-13 11:37:13
 */
public interface ActivityService {

    /**
     *  根据skuId,userId 获取促销活动+优惠券列表数据
     * @param skuId
     * @param userId
     * @return
     */
    Map<String, Object> findActivityAndCoupon(Long skuId, Long userId);

    /**
     * 获取购物车列表
     * @param cartInfoList
     * @param userId
     * @return
     */
    List<CarInfoVo> findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId);
}
