package com.changgou.search.dao;

import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Auther: hftang
 * @Date: 2020/1/2 17:08
 * @Description:
 */
@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {


}
