package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.CouponInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.activity.CouponInfo;
import com.atguigu.gmall.model.activity.CouponRuleVo;
import com.atguigu.gmall.model.enums.CouponType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-12 11:45:07
 */
@RestController
@RequestMapping("/admin/activity/couponInfo")
public class CouponInfoController {

    @Autowired
    private CouponInfoService couponInfoService;

    //  http://localhost/admin/activity/couponInfo/1/10
    @GetMapping("{page}/{limit}")
    public Result getCouponList(@PathVariable Long page,
                                @PathVariable Long limit){

        //  构建Page对象
        Page<CouponInfo> couponInfoPage = new Page<>(page,limit);
        IPage couponInfoList = couponInfoService.getCouponInfoList(couponInfoPage);
        return Result.ok(couponInfoList);
    }

    // 根据id 获取数据
    @GetMapping("get/{id}")
    public Result getCouponInfoById(@PathVariable Long id){
        CouponInfo couponInfo = couponInfoService.getById(id);
        //  设置优惠券的类型
        couponInfo.setCouponTypeString(CouponType.getNameByType(couponInfo.getCouponType()));
        //  返回数据
        return Result.ok(couponInfo);
    }

    //  保存
    @PostMapping("save")
    public Result saveCouponInfo(@RequestBody CouponInfo couponInfo){
        couponInfoService.save(couponInfo);
        return Result.ok();
    }
    //  更新
    @PutMapping("update")
    public Result updateCouponInfo(@RequestBody CouponInfo couponInfo){
        couponInfoService.updateById(couponInfo);
        return Result.ok();
    }
    //  删除  单个删除
    @DeleteMapping("remove/{id}")
    public Result removeCouponInfo(@PathVariable Long id){
        couponInfoService.removeById(id);
        return Result.ok();
    }

    //  删除  批量删除
    @DeleteMapping("batchRemove")
    public Result removeCouponInfo(@RequestBody List<Long> idList){
        couponInfoService.removeByIds(idList);
        return Result.ok();
    }

    //  保存优惠券规则
    @PostMapping("saveCouponRule")
    public Result saveCouponRule(@RequestBody CouponRuleVo couponRuleVo){
        //  调用服务层方法
        couponInfoService.saveCouponRule(couponRuleVo);
        return Result.ok();
    }

    //  http://localhost/admin/activity/couponInfo/findCouponRuleList/6
    @GetMapping("findCouponRuleList/{id}")
    public Result findCouponRuleList(@PathVariable Long id){
        //  调用方法
        Map<String,Object> map = couponInfoService.findCouponRuleList(id);
        return Result.ok(map);
    }

    @GetMapping("findCouponByKeyword/{keyword}")
    public Result findCouponByKeyword(@PathVariable String keyword){
        //  调用服务层方法
        return Result.ok(couponInfoService.findCouponByKeyword(keyword));
    }

}
