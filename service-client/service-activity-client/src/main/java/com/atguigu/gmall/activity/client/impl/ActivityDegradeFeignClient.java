package com.atguigu.gmall.activity.client.impl;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CarInfoVo;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mqx
 * @date 2021-3-8 10:54:37
 */
@Component
public class ActivityDegradeFeignClient implements ActivityFeignClient {
    @Override
    public Result findAll() {
        return null;
    }

    @Override
    public Result getSeckillGoodsById(Long skuId) {
        return null;
    }

    @Override
    public Result trade() {
        return null;
    }

    @Override
    public List<CarInfoVo> findCartActivityAndCoupon(List<CartInfo> cartInfoList, Long userId) {
        List<CarInfoVo> carInfoVoList = new ArrayList<>();
        CarInfoVo carInfoVo = new CarInfoVo();
        carInfoVo.setCartInfoList(cartInfoList);
        //  没有规则
        carInfoVo.setActivityRuleList(null);
        carInfoVoList.add(carInfoVo);
        //  返回数据
        return carInfoVoList;
    }
}
