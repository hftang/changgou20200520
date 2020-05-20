package com.changgou.user;

import entity.TokenDecode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Auther: hftang
 * @Date: 2020/2/24 10:14
 * @Description:
 */

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = "com.changgou.user.dao")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class);
    }

    //因为要查收货人的收货地址列表 要根据用户名来查询
    @Bean
    public TokenDecode tokenDecode(){
        return new TokenDecode();
    }
}
