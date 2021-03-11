package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2021-1-30 14:29:14
 */
public interface ManageService {

    //  查询所有的一级分类数据  写方法最终的是什么？ 返回值 ，参数列表
    List<BaseCategory1> getCategory1();

    //  根据选择的一级分类查询二级分类数据
    List<BaseCategory2> getCategory2(Long category1Id);

    //  根据选中的二级分类查询三级分类数据
    List<BaseCategory3> getCategory3(Long category2Id);

    //  根据分类Id{包含一级分类Id，二级分类Id，三级分类Id}获取平台属性数据
    List<BaseAttrInfo> getAttrInfoList(Long category1Id,Long category2Id,Long category3Id);


    //  大保存平台属性对象
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    //  根据平台属性Id 获取平台属性值集合
    List<BaseAttrValue> getAttrValueList(Long attrId);

    //  根据平台属性Id 查询平台属性对象
    BaseAttrInfo getAttrInfo(Long attrId);

    //  带分页的查询spuInfo列表！
    //  http://api.gmall.com/admin/product/{page}/{limit}?category3Id=61
    /**
     *
     * @param pageParam 封装查询的page,list
     * @param spuInfo 获取传递的参数条件
     * @return
     */
    IPage getSpuInfoPage(Page<SpuInfo> pageParam ,SpuInfo spuInfo);

    /**
     * 查询所有的销售属性数据
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spuInfo
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId 查询图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(Long spuId);

    /**
     * 根据spuId 查询销售属性集合数据
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * 保存skuInfo
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 查询skuInfo 列表数据
     * @param skuInfoPage
     * @return
     */
    IPage<SkuInfo> getSkuInfoList(Page<SkuInfo> skuInfoPage);

    /**
     * 商品上架
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 商品下架
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 根据skuI 查询sku基本信息+skuImage
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据category3Id 查询分类数据
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    /**
     * 查询价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 查询销售属性，销售属性值并锁定
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    // 定义数据接口： 需要得到的是json 字符串 {"106|110":40,"107|110":41} JSON.toJSONString(map);

    /**
     * 根据spuId 来获取到销售属性值Id与skuId 组成的map 集合
     * @param spuId
     * @return
     */
    Map<Object ,Object> getSkuValueIdsMap(Long spuId);

    //  获取首页数据的接口
    //  JSONObject == public class JSONObject extends JSON implements Map<String, Object>
    List<JSONObject> getBaseCategoryList();

    //  获取品牌数据
    BaseTrademark getBaseTrademarkById(Long tmId);

    //  获取平台属性,属性值数据 base_attr_info , base_attr_value , sku_attr_value
    //  根据skuId 找到对应的平台属性+平台属性值
    List<BaseAttrInfo> getAttrList(Long skuId );


}
