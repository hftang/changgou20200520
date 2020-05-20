package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/2/17 16:15
 * @Description:
 */
@FeignClient(name = "search")
@RequestMapping("/search")
public interface SkuFeign {

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */

    @GetMapping
    Map search(@RequestParam(required = false) Map searchMap);
}
