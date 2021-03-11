package com.atguigu.gmall.activity.client;

import com.atguigu.gmall.activity.client.impl.ActivityDegradeFeignClient;
import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 * @date 2021-3-8 10:54:14
 */
@FeignClient(value = "service-activity" ,fallback = ActivityDegradeFeignClient.class)
public interface ActivityFeignClient {

    @GetMapping("/api/activity/seckill/findAll")
    Result findAll();

    @GetMapping("/api/activity/seckill/getSeckillGoods/{skuId}")
    Result getSeckillGoodsById(@PathVariable Long skuId);

    @GetMapping("/api/activity/seckill/auth/trade")
    Result trade();
}
