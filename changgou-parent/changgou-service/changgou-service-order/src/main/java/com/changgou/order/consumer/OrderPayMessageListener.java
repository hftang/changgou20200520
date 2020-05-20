package com.changgou.order.consumer;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/3/11 19:44
 * @Description:
 */

@Component
@RabbitListener(queues = {"${mq.pay.queue.order}"})
public class OrderPayMessageListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void handlerData(String msg) {

        System.out.println("OrderPayMessageListener:::" + msg);

        Map<String, String> map = JSON.parseObject(msg, Map.class);

        if(map!=null){
            String return_code = map.get("return_code");
            String result_code = map.get("result_code");

            if ("success".equalsIgnoreCase(return_code)) {
                //获取单号
                String outtradeno = map.get("out_trade_no");

                if (result_code.equalsIgnoreCase("success")) {
                    if (outtradeno != null) {
                        //修改订单状态  out_trade_no
                        orderService.updateOrderStatus(outtradeno, map.get("transaction_id"));
                    }
                } else {
                    //订单删除
                    orderService.deleteOrder(outtradeno);
                }

            }
        }




    }
}
