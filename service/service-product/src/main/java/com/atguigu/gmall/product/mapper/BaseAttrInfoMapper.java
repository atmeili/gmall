package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mqx
 * @date 2021-1-30 14:27:12
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    //  编写mapper.xml 又因为传递的参数是多个，所以需要对其添加一个注解指定
    //  必须对 mybatis 有个深入的了解：
    List<BaseAttrInfo> selectBaseAttrInfoList(@Param("category1Id") Long category1Id,
                                              @Param("category2Id") Long category2Id,
                                              @Param("category3Id") Long category3Id);

    /**
     * 根据skuId 获取平台属性名+平台属性值名称等信息。
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> selectAttrListBySkuId(Long skuId);
}
