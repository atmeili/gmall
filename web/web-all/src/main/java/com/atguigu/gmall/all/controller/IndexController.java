package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author mqx
 * @date 2021-2-20 14:07:38
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    // http://www.gmall.com/index.html
    @GetMapping({"/","index.html"})
    public String index(HttpServletRequest request, Model model){
        //  链接数据库获取数据
        //  页面需要存储一个list 数据 ${list}
        Result result = productFeignClient.getBaseCategoryList();
        //  获取到的数据
        model.addAttribute("list",result.getData());
        //  返回视图名称
        return "index/index";
    }

}
