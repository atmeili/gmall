package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.mapper.ActivityInfoMapper;
import com.atguigu.gmall.activity.mapper.ActivityRuleMapper;
import com.atguigu.gmall.activity.mapper.ActivitySkuMapper;
import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.activity.ActivityRule;
import com.atguigu.gmall.model.activity.ActivityRuleVo;
import com.atguigu.gmall.model.activity.ActivitySku;
import com.atguigu.gmall.model.enums.ActivityType;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mqx
 * @date 2021-3-9 14:35:39
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {

    //  实现类中通常会引入mapper
    @Autowired
    private ActivityInfoMapper activityInfoMapper;

    @Autowired
    private ActivityRuleMapper activityRuleMapper;

    @Autowired
    private ActivitySkuMapper activitySkuMapper;

    @Override
    public IPage<ActivityInfo> getPage(Page<ActivityInfo> infoPage) {

        //  构造查询条件
        QueryWrapper<ActivityInfo> activityInfoQueryWrapper = new QueryWrapper<>();
        activityInfoQueryWrapper.orderByDesc("id");
        //  mysql 默认的排序规则？ asc ,desc
        IPage<ActivityInfo> activityInfoIPage = activityInfoMapper.selectPage(infoPage, activityInfoQueryWrapper);
        //  细节： 活动的数据类型： 在表中不存在！activityTypeString
        //  Consumer  void accept(T t);
        activityInfoIPage.getRecords().stream().forEach((activityInfo)->{
            //  如何获取到对应的数据ActivityType.getNameByType(type) ;
            activityInfo.setActivityTypeString(ActivityType.getNameByType(activityInfo.getActivityType()));
        });

        //  返回数据！
        return activityInfoIPage;
    }

    //  ActivityRuleVo ： 既包含保存，同时也可以包含修改内容！
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActivityRule(ActivityRuleVo activityRuleVo) {
        //  在写平台属性保存的时候： 先删除对应的数据，然后再新增数据！
        //  原来对应的商品活动范围列表删除：
        activitySkuMapper.delete(new QueryWrapper<ActivitySku>().eq("activity_id",activityRuleVo.getActivityId()));
        //  删除原来的活动规则列表
        activityRuleMapper.delete(new QueryWrapper<ActivityRule>().eq("activity_id",activityRuleVo.getActivityId()));
        //  优惠券列表 暂时先不写！

        //  保存数据：
        List<ActivitySku> activitySkuList = activityRuleVo.getActivitySkuList();
        List<ActivityRule> activityRuleList = activityRuleVo.getActivityRuleList();

        for (ActivitySku activitySku : activitySkuList) {
            //  需要将活动Id 赋值给当前对象
            activitySku.setActivityId(activityRuleVo.getActivityId());
            activitySkuMapper.insert(activitySku);
        }

        for (ActivityRule activityRule : activityRuleList) {
            //  需要将活动Id 赋值给当前对象
            activityRule.setActivityId(activityRuleVo.getActivityId());
            activityRuleMapper.insert(activityRule);
        }
    }

}
