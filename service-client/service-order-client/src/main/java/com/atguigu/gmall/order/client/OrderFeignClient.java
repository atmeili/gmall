package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.impl.OrderDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-1 09:45:47
 */
@FeignClient(value = "service-order",fallback = OrderDegradeFeignClient.class)
public interface OrderFeignClient {

    //  将汇总订单页面的数据接口发布到feign 上。 HttpServletRequest 主要是获取用户Id，用户Id 通过feign 拦击的方式获取到的。
    @GetMapping("api/order/auth/trade")
    Result<Map<String,Object>> trade();

    //  根据订单Id 查询订单信息+订单明细信息
    @GetMapping("api/order/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable Long orderId);

    @PostMapping("api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo);
}
