package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author mqx
 * @date 2021-2-3 09:24:49
 */
@Api(tags = "SKU的数据接口")
@RestController
@RequestMapping("admin/product")
public class SkuManageController {

    @Autowired
    private ManageService manageService;
    //  http://localhost/admin/product/saveSkuInfo
    //  获取到前端传递的数据 json ---> javaObject{要根据前端json 数据格式而定！}
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        //  调用服务层的保存方法
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/list/{page}/{limit}
    //  to {GET /admin/product/{page}/{limit}}: There is already 'skuManageController' bean method
    //  在 spu 控制器：admin/product/{page}/{limit}
    //  admin/product/{page}/{limit} 已经有了！
    @GetMapping("list/{page}/{limit}")
    public Result getSkuInfoList(@PathVariable Long page,
                                 @PathVariable Long limit){
        Page<SkuInfo> skuInfoPage = new Page<>(page,limit);
        //  调用一个查询方法 IPage Page0
        IPage<SkuInfo> infoIPage =  manageService.getSkuInfoList(skuInfoPage);
        return Result.ok(infoIPage);
    }

    //  商品上架
    // http://api.gmall.com/admin/product/onSale/{skuId}
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        //  调用方法
        manageService.onSale(skuId);
        return Result.ok();
    }

    // 商品下架
    // http://api.gmall.com/admin/product/cancelSale/{skuId}
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        //  调用方法
        manageService.cancelSale(skuId);
        return Result.ok();
    }
}
