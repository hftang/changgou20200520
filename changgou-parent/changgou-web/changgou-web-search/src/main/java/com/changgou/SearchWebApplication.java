package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @Auther: hftang
 * @Date: 2020/2/17 16:31
 * @Description:
 */

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.changgou.search.feign")
public class SearchWebApplication {


    public static void main(String[] args) {
        SpringApplication.run(SearchWebApplication.class);
    }
}
