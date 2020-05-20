package com.changgou.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.item.PageFeign;
import com.xpand.starter.canal.annotation.*;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Auther: hftang
 * @Date: 2019/12/25 18:48
 * @Description: mysql数据修改的监听
 */
@CanalEventListener
public class CanalDataEventListener {
    @Autowired
    private ContentFeign contentFeign;




    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //自定义数据库的 操作来监听
    //destination = "example"
    @ListenPoint(destination = "example",
            schema = "changgou_content",
            table = {"tb_content", "tb_content_category"},
            eventType = {
                    CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.DELETE,
                    CanalEntry.EventType.INSERT})
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //1.获取列名 为category_id的值
        String categoryId = getColumnValue(eventType, rowData);
        System.out.println("----->categoryId" + categoryId);
        //2.调用feign 获取该分类下的所有的广告集合
        Result<List<Content>> categoryresut = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> data = categoryresut.getData();
        //3.使用redisTemplate存储到redis中
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(data));
    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        String categoryId = "";
        //判断 如果是删除  则获取beforlist
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        } else {
            //判断 如果是添加 或者是更新 获取afterlist
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }
    //----------------------------下面监听spu 当页面发生改变时 生成或者删除 静态页面


    @Autowired
    private PageFeign pageFeign;

    @ListenPoint(destination = "example",
            schema = "changgou_goods",
            table = {"tb_spu"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String spuId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();//spuid
                    break;
                }
            }
            //todo 删除静态页

        } else {
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String spuId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }
    }


}
