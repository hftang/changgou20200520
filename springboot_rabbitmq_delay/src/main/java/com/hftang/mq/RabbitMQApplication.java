package com.hftang.mq;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Auther: hftang
 * @Date: 2020/3/20 18:48
 * @Description:
 */

@SpringBootApplication
@EnableRabbit
public class RabbitMQApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitMQApplication.class);
    }
}
