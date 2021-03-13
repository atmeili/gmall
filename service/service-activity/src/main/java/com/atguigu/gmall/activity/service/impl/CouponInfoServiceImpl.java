package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.CouponInfoMapper;
import com.atguigu.gmall.activity.mapper.CouponRangeMapper;
import com.atguigu.gmall.activity.mapper.CouponUseMapper;
import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRange;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.atguigu.gmall.model.activity.CouponUse;
import com.atguigu.gmall.model.enums.CouponRangeType;
import com.atguigu.gmall.model.enums.CouponStatus;
import com.atguigu.gmall.model.enums.CouponType;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mqx
 * @date 2021-3-12 11:43:16
 */
@Service
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Autowired
    private CouponRangeMapper couponRangeMapper;

    @Autowired
    private CouponUseMapper couponUseMapper;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public IPage getCouponInfoList(Page<CouponInfo> couponInfoPage) {

        //  构造排序条件
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.orderByDesc("id");
        IPage<CouponInfo> couponInfoIPage = couponInfoMapper.selectPage(couponInfoPage, couponInfoQueryWrapper);

        couponInfoIPage.getRecords().stream().forEach(couponInfo -> {
            //  赋值优惠券的类型
            couponInfo.setCouponTypeString(CouponType.getNameByType(couponInfo.getCouponType()));
            //  赋值优惠券范围
            if (couponInfo!=null){
                couponInfo.setRangeTypeString(CouponRangeType.getNameByType(couponInfo.getRangeType()));
            }
        });

        //  返回数据集合
        return couponInfoIPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCouponRule(CouponRuleVo couponRuleVo) {
        /*
        优惠券couponInfo 与 couponRange 要一起操作：先删除couponRange ，更新couponInfo ，再新增couponRange ！
         */
        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",couponRuleVo.getCouponId());
        couponRangeMapper.delete(couponRangeQueryWrapper);

        //  更新数据
        CouponInfo couponInfo = this.getById(couponRuleVo.getCouponId());
        // couponInfo.setCouponType();
        couponInfo.setRangeType(couponRuleVo.getRangeType().name());
        couponInfo.setConditionAmount(couponRuleVo.getConditionAmount());
        couponInfo.setConditionNum(couponRuleVo.getConditionNum());
        couponInfo.setBenefitDiscount(couponRuleVo.getBenefitDiscount());
        couponInfo.setBenefitAmount(couponRuleVo.getBenefitAmount());
        couponInfo.setRangeDesc(couponRuleVo.getRangeDesc());

        couponInfoMapper.updateById(couponInfo);

        //  插入优惠券的规则 couponRangeList
        List<CouponRange> couponRangeList = couponRuleVo.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            //  coupon_range.coupon_id=coupon_info.id
            couponRange.setCouponId(couponRuleVo.getCouponId());
            //  插入数据
            couponRangeMapper.insert(couponRange);
        }
    }

    @Override
    public Map<String, Object> findCouponRuleList(Long id) {
        //  声明map 集合存储数据
        Map<String, Object> map = new HashMap<>();
        //  通过id 查询 coupon_range
        QueryWrapper<CouponRange> couponRangeQueryWrapper = new QueryWrapper<>();
        couponRangeQueryWrapper.eq("coupon_id",id);
        List<CouponRange> couponRangeList = couponRangeMapper.selectList(couponRangeQueryWrapper);

        //  细节处理： 根据范围选择获取到不同的数据进行存储！
        //  coupon_range.range_id 获取到这个Id{range_id} 即可！
        List<Long> rangeIdList = couponRangeList.stream().map(CouponRange::getRangeId).collect(Collectors.toList());

        //  coupon_range ， 单独查询coupon_info
        CouponInfo couponInfo = this.getById(id);
        //  判断
        if(!CollectionUtils.isEmpty(rangeIdList)){
            //  遍历： 应该获取对应的类型！判断
            if ("SPU".equals(couponInfo.getRangeType())){
                //  根据rangeIdList 获取对应的spuInfoList
               List<SpuInfo> spuInfoList =  productFeignClient.findSpuInfoBySpuIdList(rangeIdList);
                map.put("spuInfoList",spuInfoList);
            } else if("TRADEMARK".equals(couponInfo.getRangeType())){
                List<BaseTrademark> trademarkList = productFeignClient.findBaseTrademarkByTrademarkIdList(rangeIdList);
                map.put("trademarkList",trademarkList);
            }else {
                List<BaseCategory3> category3List = productFeignClient.findBaseCategory3ByCategory3IdList(rangeIdList);
                map.put("category3List",category3List);
            }
            //            for (CouponRange couponRange : couponRangeList) {
            //                if ("SPU".equals(couponRange.getRangeType())){
            //                    //  根据rangeIdList 获取对应的spuInfoList
            //                   List<SpuInfo> spuInfoList =  productFeignClient.findSpuInfoBySpuIdList(rangeIdList);
            //                    map.put("spuInfoList",spuInfoList);
            //                } else if("TRADEMARK".equals(couponRange.getRangeType())){
            //                    List<BaseTrademark> trademarkList = productFeignClient.findBaseTrademarkByTrademarkIdList(rangeIdList);
            //                    map.put("trademarkList",trademarkList);
            //                }else {
            //                    List<BaseCategory3> category3List = productFeignClient.findBaseCategory3ByCategory3IdList(rangeIdList);
            //                    map.put("category3List",category3List);
            //                }
            //            }
        }
        //  返回数据
        return map;
    }

    @Override
    public List<CouponInfo> findCouponByKeyword(String keyword) {
        //  模糊查询
        QueryWrapper<CouponInfo> couponInfoQueryWrapper = new QueryWrapper<>();
        couponInfoQueryWrapper.like("coupon_name",keyword);
        return couponInfoMapper.selectList(couponInfoQueryWrapper);
    }

    @Override
    public List<CouponInfo> findCouponInfo(Long skuId, Long activityId, Long userId) {
        //  获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo==null) return new ArrayList<>();

        //  获取普通优惠券：与活动没有关系！
        List<CouponInfo> couponInfoList = couponInfoMapper.selectCouponInfoList(skuInfo.getSpuId(), skuInfo.getCategory3Id(), skuInfo.getTmId(), userId);
        //  activityId 判断这个优惠券与活动的关系！
        if (activityId!=null){
            //  有活动,获取优惠券列表！ 范围： spu,三级分类Id,品牌
            List<CouponInfo> activityCouponInfoList = couponInfoMapper.selectActivityCouponInfoList(skuInfo.getSpuId(), skuInfo.getCategory3Id(), skuInfo.getTmId(), activityId, userId);
            //  做一个合并
            couponInfoList.addAll(activityCouponInfoList);
        }

        return couponInfoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void getCouponInfo(Long userId, Long couponId) {
        // 判断当前优惠券是否已经领完
        CouponInfo couponInfo = this.getById(couponId);
        //  判断是否还有剩余 10
        if (couponInfo.getTakenCount()>=couponInfo.getLimitNum()){
            throw new GmallException(ResultCodeEnum.COUPON_LIMIT_GET);
        }
        // 判断这个用户是否已经领用过！
        QueryWrapper<CouponUse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id",couponId);
        queryWrapper.eq("user_id",userId);
        Integer count = couponUseMapper.selectCount(queryWrapper);
        if (count>0) {
            throw new GmallException(ResultCodeEnum.COUPON_GET);
        }
        //        CouponInfo couponInfoQuery = couponInfoMapper.selectOne(couponInfoQueryWrapper);
        //        if (couponInfoQuery!=null){
        //            throw new GmallException(ResultCodeEnum.COUPON_GET);
        //        }

        //  更新数据
        int takenCout = couponInfo.getTakenCount()+1;
        couponInfo.setTakenCount(takenCout);
        //  更新数据
        couponInfoMapper.updateById(couponInfo);

        //  插入数据到coupon_use
        CouponUse couponUse = new CouponUse();
        couponUse.setCouponId(couponId);
        couponUse.setUserId(userId);
        couponUse.setCouponStatus(CouponStatus.NOT_USED.name());
        couponUse.setGetTime(new Date());
        couponUse.setExpireTime(couponInfo.getExpireTime());

        couponUseMapper.insert(couponUse);
    }

    @Override
    public IPage<CouponInfo> getPageByUserId(Page<CouponInfo> couponInfoPage, Long userId) {
        //  coupon_info,coupon_use
        return couponInfoMapper.selectPageByUserId(couponInfoPage,userId);
    }
}
