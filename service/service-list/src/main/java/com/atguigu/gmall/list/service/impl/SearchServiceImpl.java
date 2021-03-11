package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mqx
 * @date 2021-2-22 10:09:38
 */
@Service
public class SearchServiceImpl implements SearchService {

    //  实现方法 指的是操作es。
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Autowired
    private ProductFeignClient productFeignClient;
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //  给goods 赋值
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo!=null){
            //  skuId
            goods.setId(skuInfo.getId());
            //  skuName
            goods.setTitle(skuInfo.getSkuName());
            //  price
            goods.setPrice(skuInfo.getPrice().doubleValue());

            goods.setCreateTime(new Date());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            //  赋值品牌数据
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            if (trademark!=null){
                goods.setTmId(trademark.getId());
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }

            //  赋值分类数据
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            if (categoryView!=null){
                goods.setCategory1Id(categoryView.getCategory1Id());
                goods.setCategory2Id(categoryView.getCategory2Id());
                goods.setCategory3Id(categoryView.getCategory3Id());

                goods.setCategory1Name(categoryView.getCategory1Name());
                goods.setCategory2Name(categoryView.getCategory2Name());
                goods.setCategory3Name(categoryView.getCategory3Name());
            }


            //  赋值平台属性： private List<SearchAttr> attrs; attrId，attrValue，attrName
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            //  获取数据
            if(!CollectionUtils.isEmpty(attrList)){
                //  Function R apply(T t)
                List<SearchAttr> searchAttrList = attrList.stream().map((baseAttrInfo -> {
                    SearchAttr searchAttr = new SearchAttr();
                    //  平台属性Id
                    searchAttr.setAttrId(baseAttrInfo.getId());
                    //  平台属性名称
                    searchAttr.setAttrName(baseAttrInfo.getAttrName());
                    //  平台属性值名称
                    searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return searchAttr;
                })).collect(Collectors.toList());
                //  赋值平台属性集合
                goods.setAttrs(searchAttrList);
            }
        }
        // 保存到es
        this.goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        //  下架
        this.goodsRepository.deleteById(skuId);
    }


    @Override
    public void incrHotScore(Long skuId) {
        /*
        1.  使用redis记录商品被访问的次数
            a.  采用什么数据类型
                Zset
            b.  key 如何起名

        2.  当达到一定规则的时候，更新es
         */
        String hotKey = "hotScore";

        //  ZINCRBY key increment member
        //  第一个是key，第二个是成员，第三个增长步长。
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        //  判断当前符合规则的时候10次，更新一次es
        if (hotScore%10==0){
            //  更新es
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            //  修改热度排名值
            goods.setHotScore(Math.round(hotScore));
            //  修改完成保存到es
            this.goodsRepository.save(goods);
        }
    }

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        /*
        1.  通过 java 代码动态生成dsl语句
        2.  将dsl语句查询到的结果集 封装给 SearchResponseVo
         */
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        //  进行查询操作 RestHighLevelClient ,获取到查询响应
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //  需要将这个响应结果searchResponse 封装成 SearchResponseVo
        //  trademarkList,attrsList,goodsList,total 这四个属性在parseSearchResult方法赋值
        SearchResponseVo responseVo = this.parseSearchResult(searchResponse);
        /*
        private Integer pageSize;//每页显示的内容
        private Integer pageNo;//当前页面
        private Long totalPages; // 总页数
         */
        // 给部分属性赋值
        responseVo.setPageSize(searchParam.getPageSize());
        responseVo.setPageNo(searchParam.getPageNo());
        //  在工作中总结出来的公式！
        long totalPages = (responseVo.getTotal()+searchParam.getPageSize()-1)/searchParam.getPageSize();
        responseVo.setTotalPages(totalPages);
        return responseVo;
    }

    /**
     * 数据封装
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        /*
        private List<SearchResponseTmVo> trademarkList;
        private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
        private List<Goods> goodsList = new ArrayList<>();
        private Long total;//总记录数
         */
        SearchHits hits = searchResponse.getHits();
        //  赋值品牌集合 需要从聚合中获取
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //  通过map 来获取到对应的数据 Aggregation ---> ParsedLongTerms
        //  为什么需要转换主要是想获取到buckets
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        //  Function 有参数，有返回值
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            //  什么一个品牌对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //  获取到了品牌Id
            String keyAsString = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(keyAsString));

            //  赋值品牌Name 是在另外一个桶中
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //  赋值品牌的LogoUrl
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        //  添加品牌的
        searchResponseVo.setTrademarkList(trademarkList);

        //  添加平台属性 attrAgg 属于nested 类型
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        //  在转完之后在获取attrIdAgg
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        //  获取对应的平台属性数据
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map((bucket) -> {
            //  什么一个平台属性对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //  获取到平台属性Id
            Number keyAsNumber = ((Terms.Bucket) bucket).getKeyAsNumber();
            searchResponseAttrVo.setAttrId(keyAsNumber.longValue());
            //  获取到平台属性名称
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);
            //  获取平台属性值的名称
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            //  平台属性值名称对应有多个数据 ,需要循环遍历获取到里面的每个key 所对应的数据
            List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();

            //  方式一：
            //            List<String> strings = new ArrayList<>();
            //            for (Terms.Bucket bucket1 : buckets) {
            //                //  通过key 来获取对应的数据
            //                String keyAsString = bucket1.getKeyAsString();
            //                strings.add(keyAsString);
            //            }
            //            searchResponseAttrVo.setAttrValueList(strings);
            //  方式二：
            //  表示 通过 Terms.Bucket::getKeyAsString 来获取 key
            List<String> vlaues = buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());

            searchResponseAttrVo.setAttrValueList(vlaues);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        searchResponseVo.setAttrsList(attrsList);
        // 商品集合 goodsList
        SearchHit[] subHits = hits.getHits();
        //  声明一个集合来存储Goods
        List<Goods> goodsList = new ArrayList<>();
        //  循环遍历
        for (SearchHit subHit : subHits) {
            //  是一个Goods.class 组成的json 字符串
            String sourceAsString = subHit.getSourceAsString();
            //  将sourceAsString 变为Goods的对象
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //  细节： 如果通过关键词检索，获取到高亮字段
            if(subHit.getHighlightFields().get("title")!=null){
                //  说明你是通过关键词检索的
                Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                //  覆盖原来的title
                goods.setTitle(title.toString());
            }
            goodsList.add(goods);
        }
        //  赋值商品集合对象
        searchResponseVo.setGoodsList(goodsList);
        //  赋值total
        searchResponseVo.setTotal(hits.totalHits);
        return searchResponseVo;
    }

    /**
     * 动态生成dsl语句
     * @param searchParam
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        //  构建查询器{}
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //  query -- bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //  query -- bool -- filter
        //  boolQueryBuilder.filter()
        //  判断用户是否根据分类Id 查询
        if (searchParam.getCategory1Id()!=null){
            //  query -- bool -- filter -- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }
        if (searchParam.getCategory2Id()!=null){
            //  query -- bool -- filter -- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }
        if (searchParam.getCategory3Id()!=null){
            //  query -- bool -- filter -- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }

        //  判断用户是否根据品牌过滤  query -- bool -- filter -- term
        //  trademark=2:华为
        String trademark = searchParam.getTrademark();
        //  说明用户根据品牌查询了
        if (!StringUtils.isEmpty(trademark)){
            //  大家注意：使用字符串自带的分割方法，
            //  String[] split = StringUtils.split(trademark, ":"); 不能使用spring框架的工具。 使用common.lang3
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                 boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));
            }
            // boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",2));
        }

        // 通过平台属性值进行过滤
        //  判断是否根据了平台属性值进行过滤
        //  props=23:4G:运行内存  平台属性Id 平台属性值名称 平台属性名
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            //  循环数组
            for (String prop : props) {
                //  prop = 23:4G:运行内存
                //  进行分割：
                String[] split = prop.split(":");
                //  数据符合格式
                if (split!=null && split.length==3){
                    //  创建两个bool
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //  构建查询条件
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //  将subBoolQuery 赋值到boolQuery 中
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    //  将boolQuery 赋值给总的boolQueryBuilder
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }
        //  关键字查询
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            //  query -- bool -- must -- match
            //  关键字=小米手机
            //  boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()));
            //  关键字=小米手机  小米 手机
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()).operator(Operator.AND));
        }

        //  调用query 方法 {query}
        searchSourceBuilder.query(boolQueryBuilder);

        //  分页查询
        //  表示从第几条开始查询
        int from = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(from);
        //  默认每页显示三条数据
        searchSourceBuilder.size(searchParam.getPageSize());
        //  排序
        //  判断 order=1:desc  order=1:asc
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            //  进行分割： 1:desc
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                String field = "";
                //  判断数组中第一位，如果1 按照热度排名进行排序，如果2按照价格进行排序 ...
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                //  按照这种判断标识进行排序 使用三元表达式进行判断
                searchSourceBuilder.sort(field,"asc".equals(split[1])? SortOrder.ASC:SortOrder.DESC);
            }else {
                //  默认排序规则
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }
        }
        //  高亮 必须先通过关键词检索
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //  聚合 aggs
        //  设置品牌聚合
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //  设置平台属性聚合 特殊的数据类型nested
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //  稍稍优化： 设置想要的数据字段 id，defaultImg，title，price ， 其他字段在展示的时候给你设置成null
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"},null);

        //  应该返回 SearchRequest 对象
        //  GET /goods/info/_search ||  GET /goods/_search
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);

        //  打印出来的就是dsl 语句
        System.out.println("DSL: \t"+ searchSourceBuilder.toString());
        return searchRequest;
    }
}
