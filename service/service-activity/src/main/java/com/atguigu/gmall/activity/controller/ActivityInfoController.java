package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.enums.ActivityType;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-3-9 14:37:23
 */
@RestController
@RequestMapping("/admin/activity/activityInfo")
public class ActivityInfoController {

    @Autowired
    private ActivityInfoService activityInfoService;


    @GetMapping("{page}/{limit}")
    public Result getActivityInfoList(@PathVariable Long page,
                                      @PathVariable Long limit){
        //  获取到对应的page 对象
        Page<ActivityInfo> activityInfoPage = new Page<>(page,limit);
        //  调用服务层方法
        IPage<ActivityInfo> activityInfoServicePage = activityInfoService.getPage(activityInfoPage);
        //  返回数据
        return Result.ok(activityInfoServicePage);
    }

    //  新增
    @PostMapping("save")
    public Result save(@RequestBody ActivityInfo activityInfo){
        //  保存
        activityInfo.setCreateTime(new Date());
        activityInfoService.save(activityInfo);
        return Result.ok();
    }

    //  根据Id 获取到活动对象
    //  http://localhost/admin/activity/activityInfo/get/3
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id){
        //  调用方法
        ActivityInfo activityInfo = activityInfoService.getById(id);
        //  回显数据类型失败！
        activityInfo.setActivityTypeString(ActivityType.getNameByType(activityInfo.getActivityType()));
        //  返回数据
        return Result.ok(activityInfo);
    }

    //  更新 传递数据：
    @PutMapping("update")
    public Result updateActivityInfo(@RequestBody ActivityInfo activityInfo){
        //  调用方法
        activityInfoService.updateById(activityInfo);
        //  返回数据
        return Result.ok();
    }

    //  删除数据：
    //  单个删除
    @DeleteMapping("remove/{id}")
    public Result removeById(@PathVariable Long id){
        //  删除
        activityInfoService.removeById(id);
        return Result.ok();
    }
    //  批量删除 传递的过来的Id 应该是多个
    @DeleteMapping("batchRemove")
    public Result batchRemoveById(@RequestBody List<Long> idList){
        //  批量删除数据
        activityInfoService.removeByIds(idList);
        //  返回数据
        return Result.ok();
    }

    //  保存活动规则，活动范围，优惠券列表信息
    @PostMapping("saveActivityRule")
    public Result saveActivityRule(@RequestBody ActivityRuleVo activityRuleVo){
        //  调用保存方法！
        activityInfoService.saveActivityRule(activityRuleVo);
        //  返回结果
        return Result.ok();
    }

    //  查询活动范围列表
    //  后台页面
    @GetMapping("findSkuInfoByKeyword/{keyword}")
    public Result findSkuInfoByKeyword(@PathVariable String keyword){
        //  调用服务层方法
        List<SkuInfo> skuInfoList = activityInfoService.findSkuInfoByKeyword(keyword);
        return Result.ok(skuInfoList);
    }

    //  回显活动数据：
    @GetMapping("findActivityRuleList/{id}")
    public Result findActivityRuleList(@PathVariable Long id){
        //  调用服务层方法
        Map<String,Object> map =  activityInfoService.findActivityRuleList(id);
        return Result.ok(map);
    }
}
