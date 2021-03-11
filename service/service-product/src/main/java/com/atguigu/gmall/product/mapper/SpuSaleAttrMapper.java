package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-2 11:44:15
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * 根据spuId 查询销售属性集合数据
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(Long spuId);

    /**
     * 查询销售属性数据+并锁定
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(@Param("skuId") Long skuId, @Param("spuId")Long spuId);
}
