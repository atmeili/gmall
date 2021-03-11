package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.apache.ibatis.io.ResolverUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author mqx
 * @date 2021-2-1 15:59:28
 */
@Api(tags = "SPU的数据接口")
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;
    //  http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 SpuInfo spuInfo){

        // 创建一个Page 对象
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        IPage serviceSpuInfoPage = manageService.getSpuInfoPage(spuInfoPage, spuInfo);
        //  放入查询之后的数据集
        return Result.ok(serviceSpuInfoPage);
    }

    //  http://localhost/admin/product/baseSaleAttrList
    //  查询销售属性列表
    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList(){
        //  返回数据
        return Result.ok(manageService.getBaseSaleAttrList());
    }

    //  http://localhost/admin/product/saveSpuInfo
    //  后台接收传递的参数Json 字符串，然后转换为java 对象
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        //  调用保存方法
        manageService.saveSpuInfo(spuInfo);
        //  返回数据
        return Result.ok();
    }

    // http://localhost/admin/product/spuImageList/25
    @GetMapping("spuImageList/{spuId}")
    public Result getSpuImageList(@PathVariable Long spuId){
        //  调用服务层方法
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    //  http://api.gmall.com/admin/product/spuSaleAttrList/{spuId}
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result getSpuSaleAttrList(@PathVariable Long spuId){

        //  调用服务层方法 返回值需要包含平台属性名 对应多个平台属性值的名称
        //  颜色： red ，yellow  版本： 6+64 6+128  1:n
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }



}
