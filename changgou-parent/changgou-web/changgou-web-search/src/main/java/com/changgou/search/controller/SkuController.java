package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/2/17 16:22
 * @Description:
 */
@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {

        Map resultMap = this.skuFeign.search(searchMap);
        //结果
        model.addAttribute("result", resultMap);
        //条件
        model.addAttribute("searchMap", searchMap);

        //把url再次放入

        String url = urlMethod(searchMap);

        model.addAttribute("url", url);

        //page
        Page<SkuInfo> infoPage = new Page<SkuInfo>(Integer.parseInt(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNum").toString()),
                Integer.parseInt(resultMap.get("pageSize").toString()));

        model.addAttribute("page",infoPage);


        return "search";
    }

    /**
     * 获取url的方法
     *
     * @param resultMap
     * @return
     */

    private String urlMethod(Map<String, String> resultMap) {

        String url = "/search/list";
        if (resultMap != null && resultMap.size() > 0) {
            url += "?";
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.equals("sortField") || key.equals("sortRule")) {
                    continue;
                }

                if(key.equalsIgnoreCase("pageNum")){
                    continue;
                }

                url += key + "=" + value + "&";

            }
            //去掉最后一个&
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }


}
