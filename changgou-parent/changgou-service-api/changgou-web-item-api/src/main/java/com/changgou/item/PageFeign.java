package com.changgou.item;

import entity.Result;
import entity.StatusCode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Auther: hftang
 * @Date: 2020/2/21 17:40
 * @Description: 针对 item 生成静态页面 对外调用接口
 */


@FeignClient(name = "item")
@RequestMapping("/page")
public interface PageFeign {


    /**
     * 生成静态页面
     *
     * @param id
     * @return
     */
    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name = "id") Long id);


}
