package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRule;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-9 14:34:44
 */
//  后面我们会对这个接口进行一系列的CURD 操作！
public interface ActivityInfoService extends IService<ActivityInfo> {

    //  带分页的数据查询！
    IPage<ActivityInfo> getPage(Page<ActivityInfo> infoPage);

    //  保存数据：
    void saveActivityRule(ActivityRuleVo activityRuleVo);

    /**
     * 根据关键词检索数据
     * @param keyword
     * @return
     */
    List<SkuInfo> findSkuInfoByKeyword(String keyword);

    /**
     * 获取数据
     * @param activityId
     * @return
     */
    Map<String, Object> findActivityRuleList(Long activityId);

    /**
     * 根据skuId 获取促销活动列表
     * @param skuId
     * @return
     */
    List<ActivityRule> findActivityRule(Long skuId);


}
