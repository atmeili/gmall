package com.atguigu.gmall.model.cart;

import com.atguigu.gmall.model.activity.ActivityRule;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CarInfoVo implements Serializable {
   
   private static final long serialVersionUID = 1L;

   /**
    * 购物车中哪些skuId对应同一组活动规则
    * 如：skuId为1与2的购物项  对应  活动1的规则 （满1000减100 满2000减200）
    */
   // 存储同一组活动的购物项：
   @ApiModelProperty(value = "cartInfoList")
   private List<CartInfo> cartInfoList;

   // activityRuleList 存储的是同一个活动规则列表
   @ApiModelProperty(value = "活动规则列表")
   private List<ActivityRule> activityRuleList;

   // 1，2 这种情况： cartInfoList有数据{小米10 44 ，小米10 33}  activityRuleList 记录活动规则
   // 3，4 这种情况： cartInfoList有数据{iphone-11 ，iphonexr-33}  activityRuleList 没有数据记录


}