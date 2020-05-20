package com.changgou.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Auther: hftang
 * @Date: 2020/2/20 19:00
 * @Description:
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.changgou.goods.feign")
public class ItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class);
    }
}
