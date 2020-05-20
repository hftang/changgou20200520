package com.hftang.mq.ttt;

import com.hftang.mq.config.QueueConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/3/20 20:52
 * @Description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Demo01 {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void run01() throws IOException {
        Map<String, String> Msg = new HashMap<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        Msg.put("currentTime", format);
        Msg.put("msg", "发送一个事件");

        this.rabbitTemplate.convertAndSend(QueueConfig.QUEUE_MESSAGE_DELAY, Msg, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) {
                message.getMessageProperties().setExpiration("1000");

                return message;
            }
        });
        System.out.println("发送完成");

        System.in.read();
    }


}
