package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 * @date 2021-3-3 11:53:50
 */
@Controller
public class PaymentController {

    //  第二种方案：
    @Autowired
    private OrderFeignClient orderFeignClient;

    //  http://payment.gmall.com/pay.html?orderId=139
    @GetMapping("pay.html") // Long orderId,
    public String pay( HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        //  通过页面可以得出结论： 存储一个orderInfo
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(Long.parseLong(orderId));
        request.setAttribute("orderInfo",orderInfo);
        return "payment/pay";
    }

    //  http://payment.gmall.com/pay/success.html
    @RequestMapping("pay/success.html")
    public String success(){
        //  返回支付成功页面
        return "payment/success";
    }
}
