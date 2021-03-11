package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author mqx
 * @date 2021-2-3 16:00:58
 */
@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    //  发布一个远程调用地址准备给 web-all 使用！
    @GetMapping("{skuId}")
    public Result getItem(@PathVariable Long skuId){
        // 获取封装之后的数据集
        Map<String, Object> result = itemService.getBySkuId(skuId);
        //  将map 赋值给result 对象中的data 属性
        return Result.ok(result);
    }
}
