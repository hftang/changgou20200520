package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: hftang
 * @Date: 2020/1/2 16:37
 * @Description:
 */

@FeignClient(value = "goods")
@RequestMapping(value = "/sku")
public interface SkuFeign {

    /****
     *      * 库存递减
     *      * @param username
     *      void decrCount(String username);
     */

    @PostMapping(value = "/decr/count")
    public Result decrCount(@RequestParam(value = "username") String username);

    /***
     * 根据审核状态查询Sku
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable(name = "status") String status);

    /**
     * 根据spuid 获取 sku集合
     */

    @PostMapping(value = "/search")
    public Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /**
     * 根据id查找sku
     */

    @GetMapping(value = "/{id}")
    public Result<Sku> findById(@PathVariable(value = "id", required = true) Long id);


}
