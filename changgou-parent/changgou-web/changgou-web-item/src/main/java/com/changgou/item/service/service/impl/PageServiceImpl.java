package com.changgou.item.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/2/20 20:17
 * @Description:
 */
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private CategoryFeign categoryFeign;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagepath}")
    private String pagepath;

    /**
     * 根据spuid生成产品详情页
     *
     * @param spuId
     */

    @Override
    public void createPageHtml(Long spuId) {

        //thyleaf 的上下文
        Context context = new Context();
        Map<String, Object> dataModel = buildDataModel(spuId);
        context.setVariables(dataModel);

        File dir = new File(pagepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, spuId + ".html");

        Writer writer= null;
        try {
            writer = new PrintWriter(file,"UTF-8");
            this.templateEngine.process("item",context,writer);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 组装数据
     *
     * @param spuId
     * @return
     */
    private Map<String, Object> buildDataModel(Long spuId) {

        Map<String, Object> dataResult = new HashMap<>();

        //获取 spu 和sku列表
        Result<Spu> spuResult = this.spuFeign.findById(spuId);
        Spu spu = spuResult.getData();

        //获取分类信息
        dataResult.put("category1", this.categoryFeign.findById(spu.getCategory1Id()).getData());
        dataResult.put("category2", this.categoryFeign.findById(spu.getCategory2Id()).getData());
        dataResult.put("category3", this.categoryFeign.findById(spu.getCategory3Id()).getData());

        if (spu.getImages() != null) {
            dataResult.put("imageList", spu.getImages().split(","));
        }

        dataResult.put("specificationList", JSON.parseObject(spu.getSpecItems(), Map.class));

        dataResult.put("spu", spu);

        //sku

        Sku skuCondition = new Sku();
        skuCondition.setSpuId(spuId);
        Result<List<Sku>> skuFeignList = this.skuFeign.findList(skuCondition);
        dataResult.put("skuList", skuFeignList.getData());

        return dataResult;
    }
}
