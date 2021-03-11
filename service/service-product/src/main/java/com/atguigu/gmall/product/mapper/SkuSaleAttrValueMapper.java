package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-2-3 09:34:08
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    //  根据spuId 获取数据
    List<Map> selectSaleAttrValuesBySpu(Long spuId);
}
