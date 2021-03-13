package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author mqx
 * @date 2021-3-13 15:34:34
 */
@Controller
public class ActivityController {

    //  自定义一个控制器
    @GetMapping("couponInfo.html")
    public String couponInfo(){

        //  返回优惠券列表
        return "couponInfo/index";
    }
}
