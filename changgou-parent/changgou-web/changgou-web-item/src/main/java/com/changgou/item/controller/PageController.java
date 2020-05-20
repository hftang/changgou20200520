package com.changgou.item.controller;

import com.changgou.item.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: hftang
 * @Date: 2020/2/20 20:13
 * @Description:
 */
@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired
    PageService pageService;


    /**
     * 生成静态页面
     * @param id
     * @return
     */
    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name = "id") Long id) {

        this.pageService.createPageHtml(id);

        return new Result(true, StatusCode.OK, "生成静态页成功");
    }


}
