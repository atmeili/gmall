package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author mqx
 * @date 2021-2-4 15:14:36
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {


    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
