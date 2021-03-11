package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mqx
 * @date 2021-1-30 15:48:24
 */
@Api(tags = "后台数据接口")
@RestController
@RequestMapping("admin/product")
public class BaseManageController {

    @Autowired
    private ManageService manageService;

    @GetMapping("getCategory1")
    public Result getCategory1(){
        //  获取所有的一级分类数据
        List<BaseCategory1> baseCategory1List = manageService.getCategory1();
        //  返回数据
        return Result.ok(baseCategory1List);
    }

    //  根据一级分类Id 查询二级分类数据
    // http://api.gmall.com/admin/product/getCategory2/{category1Id}
    @GetMapping("getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable Long category1Id){

        //  调用服务层方法
        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);

        return  Result.ok(baseCategory2List);

    }

    //  根据二级分类Id 查询三级分类数据
    //  http://api.gmall.com/admin/product/getCategory3/{category2Id}
    @GetMapping("getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable Long category2Id){

        //  调用服务层方法
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);

        return  Result.ok(baseCategory3List);
    }
    //  根据分类Id 查询平台属性数据
    // http://api.gmall.com/admin/product/attrInfoList/{category1Id}/{category2Id}/{category3Id}
    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(@PathVariable Long category1Id,
                               @PathVariable Long category2Id,
                               @PathVariable Long category3Id){

        //  调用后台服务层方法
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);

        return Result.ok(attrInfoList);
    }

    //  保存平台属性
    //  http://api.gmall.com/admin/product/saveAttrInfo
    //  在这个控制器接收Json 数据 ，同时后台代码能否处理Json 数据
    //  @RequestBody 将Json ---> JavaObject json 数据转换为java对象。
    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        //  调用服务层方法
        manageService.saveAttrInfo(baseAttrInfo);
        //  默认返回data = null
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/getAttrValueList/{attrId}
    @GetMapping("getAttrValueList/{attrId}")
    public Result getAttrValueList(@PathVariable Long attrId){
        //  调用服务层方法,判断是否有平台属性？
        //  如果有平台属性，那么就查询平台属性值，如果没有平台属性，不查询！
        BaseAttrInfo baseAttrInfo =  manageService.getAttrInfo(attrId);

        //  List<BaseAttrValue> baseAttrValueList =  manageService.getAttrValueList(attrId);
        //  返回数据
        //  return Result.ok(baseAttrValueList);
        //  同样也返回平台属性值集合，但是这个集合是从平台属性对象中获取的！
        return Result.ok(baseAttrInfo.getAttrValueList());
    }

}
