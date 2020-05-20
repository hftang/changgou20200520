package com.hftang.mq.listener;

import com.hftang.mq.config.QueueConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: hftang
 * @Date: 2020/3/20 20:48
 * @Description:
 */

@Component
@RabbitListener(queues = QueueConfig.QUEUE_MESSAGE)
public class MessageListener {

    @RabbitHandler
    public void msg(@Payload Object obj){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("当前时间："+simpleDateFormat.format(new Date()));
        System.out.println("收到的数据："+obj);
    }
}
