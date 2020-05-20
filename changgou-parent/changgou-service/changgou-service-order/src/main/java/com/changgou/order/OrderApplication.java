package com.changgou.order;

import com.changgou.framework.intercepter.FeignInterceptor;
import entity.IdWorker;
import entity.TokenDecode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Auther: hftang
 * @Date: 2020/3/3 10:49
 * @Description:
 */

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.changgou.goods.feign","com.changgou.user.feign"})
@MapperScan(basePackages = "com.changgou.order.dao")
@EnableEurekaClient
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public FeignInterceptor feignInterceptor() {

        return new FeignInterceptor();
    }

    @Bean
    public TokenDecode tokenDecode() {

        return new TokenDecode();
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }
}
