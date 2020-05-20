package com.changgou.seckill.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/3/20 10:38
 * @Description: 监听秒杀队列
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")  //exchange.seckillorder  queue.seckillorder
public class SeckillOrderPayMessageListener {
    @Autowired
    SeckillOrderService seckillOrderService;




    /**
     * 监听消费消息
     *
     * @param message
     */
    @RabbitHandler
    public void consumeMessage(@Payload String message) {
        System.out.println(message);
        //将消息转换成Map对象
        Map<String, String> resultMap = JSON.parseObject(message, Map.class);
        System.out.println("监听到的消息:" + resultMap);


        String return_code = resultMap.get("return_code");
        String result_code = resultMap.get("result_code");

        if ("success".equalsIgnoreCase(return_code)) {

            String outTradeNo = resultMap.get("out_trade_no");

            //获取附加信息
            Map<String, String> attachMap = JSONObject.parseObject(resultMap.get("attach"), Map.class);

            //如果是支付成功

            if ("success".equalsIgnoreCase(result_code)) {
                //修改订单状态
                seckillOrderService.updatePayStatus(outTradeNo,attachMap.get("transaction_id"),attachMap.get("username"));
            } else {
                //支付失败 删除订单
                //支付失败,删除订单
                seckillOrderService.closeOrder(attachMap.get("username"));
            }
        }


    }
}
