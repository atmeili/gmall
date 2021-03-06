package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-2 09:11:45
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper,BaseTrademark> implements BaseTrademarkService {

    // 服务层调用mapper 层
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;



    @Override
    public IPage<BaseTrademark> getPage(Page<BaseTrademark> baseTrademarkPage) {
        //  构造查询的条件，构造排序功能
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.orderByDesc("id");

        return baseTrademarkMapper.selectPage(baseTrademarkPage,baseTrademarkQueryWrapper);
    }

    @Override
    public List<BaseTrademark> findBaseTrademarkByKeyword(String keyword) {

        //  创建查询条件
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.like("tm_name",keyword);
        return baseTrademarkMapper.selectList(baseTrademarkQueryWrapper);
    }

    @Override
    public List<BaseTrademark> findBaseTrademarkByTrademarkIdList(List<Long> trademarkIdList) {
        return baseTrademarkMapper.selectBatchIds(trademarkIdList);
    }

}
