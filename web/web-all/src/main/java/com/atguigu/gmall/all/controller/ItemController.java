package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author mqx
 * @date 2021-2-4 15:18:08
 */
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    //  用户访问的时候 http://item.gmall.com/40.html
    //  http://item.gmall.com/41.html
    @GetMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model){
        //  获取远程传递的数据
        Result<Map> result = itemFeignClient.getItem(skuId);
        //  获取到result 中的data 它才是我们想要的map 封装好的数据！
        //  model.addAttribute("name","刘德华");
        //  用来存储所有数据Map 集合 map的key 就是页面要渲染的key！
        model.addAllAttributes(result.getData());
        //  返回页面视图名称
        return "item/index";
    }
}
