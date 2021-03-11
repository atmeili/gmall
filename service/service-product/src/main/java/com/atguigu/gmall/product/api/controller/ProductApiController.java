package com.atguigu.gmall.product.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-2-4 09:10:02
 */
@RestController
@RequestMapping("api/product") // 定一个内部发送数据接口url 的入口
public class ProductApiController {

    //  引入服务层对象
    @Autowired
    private ManageService manageService;

    //  定义一个具体的url 路径
    //  查询skuInfo 数据
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        //  调用服务层方法
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //  这个skuInfo 中，只有自己的信息以及 skuImage集合数据
        return skuInfo;
    }

    // 查询分类数据
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        //  调用服务层方法返回数据
        return manageService.getCategoryViewByCategory3Id(category3Id);
    }

    //  查询最新价格数据
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        return manageService.getSkuPrice(skuId);
    }

    //  获取回显销售属性并锁定数据
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        //  返回数据
        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    //  根据spuId 获取销售属性值与skuId 组合成的map 数据
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }

    //  获取首页数据
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList(){

        List<JSONObject> baseCategoryList = manageService.getBaseCategoryList();

        return Result.ok(baseCategoryList);
    }

    //  获取品牌数据
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable Long tmId){
        return manageService.getBaseTrademarkById(tmId);
    }

    //  根据skuId 获取平台属性数据
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable Long skuId){
        return manageService.getAttrList(skuId);
    }

}

