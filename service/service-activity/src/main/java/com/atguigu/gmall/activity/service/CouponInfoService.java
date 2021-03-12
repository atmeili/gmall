package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-12 11:42:51
 */
public interface CouponInfoService extends IService<CouponInfo> {


    //  带分页的数据查询
    IPage getCouponInfoList(Page<CouponInfo> couponInfoPage);

    /**
     * 大保存
     * @param couponRuleVo
     */
    void saveCouponRule(CouponRuleVo couponRuleVo);

    /**
     * 优惠券范围
     * @param id
     * @return
     */
    Map<String, Object> findCouponRuleList(Long id);

    /**
     *
     * @param keyword
     * @return
     */
    List<CouponInfo> findCouponByKeyword(String keyword);
}
