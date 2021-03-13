package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.model.activity.CouponInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    /**
     * 普通优惠券
     * @param spuId
     * @param category3Id
     * @param tmId
     * @param userId
     * @return
     */
    List<CouponInfo> selectCouponInfoList(@Param("spuId") Long spuId, @Param("category3Id")Long category3Id, @Param("tmId")Long tmId, @Param("userId")Long userId);

    /**
     * 活动优惠券
     * @param spuId
     * @param category3Id
     * @param tmId
     * @param activityId
     * @param userId
     * @return
     */
    List<CouponInfo> selectActivityCouponInfoList(@Param("spuId") Long spuId, @Param("category3Id")Long category3Id, @Param("tmId")Long tmId, @Param("activityId")Long activityId, @Param("userId")Long userId);

    /**
     * 根据用户Id 查询优惠券列表
     * @param couponInfoPage
     * @param userId
     * @return
     */
    IPage<CouponInfo> selectPageByUserId(Page<CouponInfo> couponInfoPage, @Param("userId") Long userId);
}