package com.changgou.content.feign;
import com.changgou.content.pojo.Content;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(value="content")
public interface ContentFeign {
    /*
     根据分类的ID 获取到广告列表
      */
    @GetMapping(value = "/content/list/category/{cid}")
    Result<List<Content>> findByCategory(@PathVariable(name="cid") Long cid);
}