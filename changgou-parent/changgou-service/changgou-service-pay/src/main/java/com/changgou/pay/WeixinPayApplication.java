package com.changgou.pay;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @Auther: hftang
 * @Date: 2020/3/8 16:45
 * @Description:
 */

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
public class WeixinPayApplication {
    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class, args);
    }


    //创建交换机

    @Bean
    public DirectExchange basicExchanage() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"), true, false);
    }


    //创建队列 queue.order
    @Bean(name = "queueOrder")
    public Queue queueOrder() {
        return new Queue(env.getProperty("mq.pay.queue.order"), true);
    }


    /****
     * 队列绑定到交换机上
     * @return  queue.order
     */
    @Bean
    public Binding basicBindingOrder(){
        return BindingBuilder.bind(queueOrder()).to(basicExchanage()).with(env.getProperty("mq.pay.routing.key"));
    }

    //---------------------------------------------------------------


    //创建交换机 kill秒杀

    @Bean
    public DirectExchange basicKillExchanage() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"), true, false);
    }

    /***
     * 创建秒杀队列
     * @return  queue.seckillorder
     */

    @Bean(name = "queueSeckillOrder")
    public Queue queueSeckillOrder() {
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"), true);
    }
    /****
     * 队列绑定到交换机上
     * @return  queue.seckillorder
     */
    @Bean
    public Binding basicBindingSeckillOrder(){
       return BindingBuilder.bind(queueSeckillOrder()).to(basicKillExchanage()).with(env.getProperty("mq.pay.routing.seckillkey"));
    }




}
