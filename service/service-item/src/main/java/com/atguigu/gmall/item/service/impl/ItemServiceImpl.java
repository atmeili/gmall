package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mqx
 * @date 2021-2-3 15:55:51
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ListFeignClient listFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result = new HashMap<>();

        //  使用异步编排优化商品详情渲染！
        //  创建一个异步编排对象
        //  Supplier T get();
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            //  执行任务
            //  skuInfo+skuImage集合 数据
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            //  将数据封装到map中
            result.put("skuInfo",skuInfo);
            return skuInfo;
        },threadPoolExecutor);

        //  因为获取到分类数据之后，只需要放入map 中即可，后续没有任何方法使用到该返回值
        //  Consumer void accept(T t);
//        skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {
//
//        }))
//        skuInfoCompletableFuture.thenAcceptAsync(skuInfo->{
//
//        });

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            //  获取分类数据
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            //  放入map 中
            result.put("categoryView", categoryView);
        },threadPoolExecutor);

        //  获取到价格
        CompletableFuture<Void> skuPriceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            result.put("price", skuPrice);
        },threadPoolExecutor);

        //  获取到回显的销售属性+销售属性值+锁定功能数据
        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        },threadPoolExecutor);

        //  有关于用户点击销售属性值切换的功能！
        CompletableFuture<Void> skuValueIdsCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //  因为前端需要的是Json 数据，因此在这个位置我们需要将Map 转换为Json 数据
            String mapJson = JSON.toJSONString(skuValueIdsMap);
            System.out.println(mapJson);
            result.put("valuesSkuJson", mapJson);
        },threadPoolExecutor);

        //  使用异步编排调用热度排名
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);


        //  任务组合：
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewCompletableFuture,
                skuPriceCompletableFuture,
                spuSaleAttrCompletableFuture,
                skuValueIdsCompletableFuture,
                completableFuture
        ).join();
        // 返回map 集合
        return result;
    }
}
