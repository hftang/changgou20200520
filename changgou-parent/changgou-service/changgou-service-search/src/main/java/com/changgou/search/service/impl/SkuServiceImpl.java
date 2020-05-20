package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.PageResult;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Auther: hftang
 * @Date: 2020/1/2 17:10
 * @Description:
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ElasticsearchTemplate esTemplate;


    @Override
    public void importSku() {

        Result<List<Sku>> data = this.skuFeign.findByStatus("1");

        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(data.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(specMap);
        }

//        this.skuEsMapper.saveAll(skuInfos);

        this.skuEsMapper.saveAll(skuInfos);
    }


    @Override
    public Map search(Map<String, String> searchMap) {


        //1.获取关键字的值
        String keywords = searchMap.get("keywords");

        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();


        //3.设置查询的条件

        //设置分组条件  商品分类
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));

        //设置分组条件 商品品牌

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));

        //规格参数
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(50));

        //设置查询条件
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords, "name", "brandName", "categoryName"));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
        }
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
        }


        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);

        //规格过滤查询
        if (searchMap != null) {
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }
        }

        //过滤价格区间

        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");
            if (!split[1].equalsIgnoreCase("*")) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }

        //构建分页
        Integer pageNum = 1;
        if (!StringUtils.isEmpty(searchMap.get("pageNum"))) {
            try {
                pageNum = Integer.valueOf(searchMap.get("pageNum"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                pageNum = 1;
            }
        }

        //第一个参数:指定当前的页码  注意: 如果是第一页 数值为0
        //第二个参数:指定当前的页的显示的行
//        String pageNum1 = searchMap.get("pageNum");
//        Integer pageNum=Integer.valueOf(pageNum1);

        Integer pageSize=30;


        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum, pageSize));

        //排序组合
        String sortRule = searchMap.get("sortRule");
        String sortField = searchMap.get("sortField");

        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equals("DESC") ? SortOrder.DESC : SortOrder.ASC));
        }
        //设置高亮

        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));


        //构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //执行查询
        AggregatedPage<SkuInfo> skuInfos = this.esTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapperImpl());

        StringTerms stringTermsCategory = (StringTerms) skuInfos.getAggregation("skuCategorygroup");
        //获取品牌分组结果
        StringTerms skuBrandgroup = (StringTerms) skuInfos.getAggregation("skuBrandgroup");
        StringTerms skuSpecgroup = (StringTerms) skuInfos.getAggregation("skuSpecgroup");

        List<String> brandList = getStringsBrandNameList(skuBrandgroup);
        List<String> categoryList = getStringsCategoryList(stringTermsCategory);
        Map<String, Set<String>> specMap = getStringSetMap(skuSpecgroup);




        Map resultMap = new HashMap();

        resultMap.put("specMap", specMap);
        resultMap.put("brandList", brandList);
        resultMap.put("categoryList", categoryList);
        resultMap.put("rows", skuInfos.getContent());
        resultMap.put("total", skuInfos.getTotalElements());
        resultMap.put("totalPages", skuInfos.getTotalPages());

        resultMap.put("pageNum",pageNum);
        resultMap.put("pageSize",pageSize);

        return resultMap;
    }

    private List<String> getStringsBrandList(StringTerms stringTermsBrand) {

        List<String> brandList=new ArrayList<>();

        if(stringTermsBrand!=null){
            for (StringTerms.Bucket bucket : stringTermsBrand.getBuckets()) {
                brandList.add(bucket.getKeyAsString());
            }
        }
        return brandList;
    }

    /**
     * 获取规格参数
     *
     * @param skuSpecgroup
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(StringTerms skuSpecgroup) {
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specList = new HashSet<>();
        if (skuSpecgroup != null) {

            for (StringTerms.Bucket bucket : skuSpecgroup.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                specList.add(keyAsString);
            }
        }

        for (String specjson : specList) {
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {//
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();    //规格选项值
                //获取当前规格名字对应的规格数据
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }

    /**
     * 获取商品的名字
     *
     * @param skuBrandgroup
     * @return
     */

    private List<String> getStringsBrandNameList(StringTerms skuBrandgroup) {
        if (skuBrandgroup != null) {
            List<String> arrayList = new ArrayList<>();
            for (StringTerms.Bucket bucket : skuBrandgroup.getBuckets()) {
                arrayList.add(bucket.getKeyAsString());
            }

            return arrayList;
        }

        return null;
    }


    private List<String> getStringsCategoryList(StringTerms stringTerms) {

        List<String> categoryList = new ArrayList<>();
        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//分组的值
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }
}
