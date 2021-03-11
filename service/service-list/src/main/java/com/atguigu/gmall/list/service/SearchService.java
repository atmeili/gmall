package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

/**
 * @author mqx
 * @date 2021-2-22 10:07:45
 */
public interface SearchService {

    //  商品的上架： 根据实实在在的skuId 进行商品上架
    void upperGoods(Long skuId);

    //  商品的下架：根据实实在在的skuId 进行商品下架
    void lowerGoods(Long skuId);

    //  商品的热度排名
    void incrHotScore(Long skuId);

    //  检索数据接口
    SearchResponseVo search(SearchParam searchParam) throws IOException;


}
