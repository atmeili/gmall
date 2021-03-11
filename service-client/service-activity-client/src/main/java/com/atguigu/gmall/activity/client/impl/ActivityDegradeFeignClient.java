package com.atguigu.gmall.activity.client.impl;

import com.atguigu.gmall.activity.client.ActivityFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.stereotype.Component;

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
}
