package com.changgou.goods;

import com.changgou.framework.intercepter.FeignInterceptor;
import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Auther: hftang
 * @Date: 2019/12/22 15:46
 * @Description:
 */
@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = "com.changgou.goods.dao")
public class GoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class);
    }

    /**
     * id生成器
     *
     * @return
     */
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(0, 0);
    }

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }


}
