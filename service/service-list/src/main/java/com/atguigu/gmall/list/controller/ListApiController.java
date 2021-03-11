package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author mqx
 * @date 2021-2-20 15:51:06
 */
@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;

    @GetMapping("inner/createIndex")
    public Result index(){

        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        //  返回数据
        return Result.ok();
    }

    //  上架
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){
        searchService.upperGoods(skuId);
        return  Result.ok();
    }

    // 下架
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){
        searchService.lowerGoods(skuId);
        return  Result.ok();
    }

    // 商品的热度排名
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable Long skuId){
        searchService.incrHotScore(skuId);
        return  Result.ok();
    }

    /**
     * 检索控制器
     * @param searchParam 用户输入的检索条件 Json 对象
     * @return
     * @throws IOException
     */
    @PostMapping
    public Result getList(@RequestBody SearchParam searchParam) throws IOException {
        SearchResponseVo searchResponseVo = searchService.search(searchParam);
        return Result.ok(searchResponseVo);
    }

}
