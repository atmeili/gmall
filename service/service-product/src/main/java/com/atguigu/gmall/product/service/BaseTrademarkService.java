package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-2 09:08:53
 */
public interface BaseTrademarkService extends IService<BaseTrademark> {

    //  编写接口： 查询品牌的分页列表
    //  需要传入 page,limit
    //  service 服务层接口 get save 等词语。 如果是mapper层， insert ，select ,delete等词语开头。
    IPage<BaseTrademark> getPage(Page<BaseTrademark> baseTrademarkPage);

    /**
     * 根据关键字查询
     * @param keyword
     * @return
     */
    List<BaseTrademark> findBaseTrademarkByKeyword(String keyword);

    /**
     *
     * @param trademarkIdList
     * @return
     */
    List<BaseTrademark> findBaseTrademarkByTrademarkIdList(List<Long> trademarkIdList);


    //  save 方法

    //  remove 方法
}
