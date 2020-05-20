package com.changgou.seckill;

import entity.IdWorker;
import entity.TokenDecode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Auther: hftang
 * @Date: 2020/3/12 17:02
 * @Description:
 */

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@MapperScan(basePackages = {"com.changgou.seckill.dao"})
@EnableScheduling //开启定时器
@EnableAsync
public class SecKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecKillApplication.class, args);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }
    @Bean
    public TokenDecode tokenDecode(){
        return new TokenDecode();
    }
}
