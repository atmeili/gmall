package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-1 09:50:55
 */
@Controller
public class OrderController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    //  http://order.gmall.com/trade.html
    @GetMapping("trade.html")
    public String trade(Model model){
        Result<Map<String, Object>> result = orderFeignClient.trade();
        //  需要将数据放入作用域中
        model.addAllAttributes(result.getData());
        //  返回视图名称
        return "order/trade";
    }
}
