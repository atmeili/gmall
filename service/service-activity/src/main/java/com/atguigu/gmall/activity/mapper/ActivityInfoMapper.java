package com.atguigu.gmall.activity.mapper;

import com.atguigu.gmall.model.activity.ActivityInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mqx
 * @date 2021-3-9 14:30:33
 */
@Mapper
public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {

    //  根据skuId集合获取对应哪些skuId 是在参与活动！
    List<Long> selectExistSkuIdList(@Param("skuIdList") List<Long> skuIdList);
}
