package com.changgou.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Auther: hftang
 * @Date: 2020/3/20 11:57
 * @Description:
 */
@FeignClient(name = "seckill")
public interface SeckillOrderFeign {
}
