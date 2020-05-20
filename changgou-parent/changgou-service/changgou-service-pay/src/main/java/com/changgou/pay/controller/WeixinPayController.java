package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/3/10 16:48
 * @Description:
 */
@RestController
@RequestMapping(value = "/weixin/pay")
public class WeixinPayController {

    @Autowired
    private WeixinPayService weixinPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${mq.pay.exchange.order}")
    private String exchange;
    @Value("${mq.pay.queue.order}")
    private String queue;
    @Value("${mq.pay.routing.key}")
    private String routing;

    /**
     * String outtradeno, String money
     *
     * @param
     * @return
     */

    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String, String> parameter) {

        Map map = this.weixinPayService.createNative(parameter);
        return new Result(true, StatusCode.OK, "创建二维码预付订单成功！", map);

    }

    @RequestMapping(value = "/status/query")
    public Result queryOrderStatus(String outtradeno) {
        Map map = this.weixinPayService.queryPayStatus(outtradeno);
        return new Result(true, StatusCode.OK, "查询支付状态成功", map);
    }

    /**
     * 支付成功之后的回调
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) {

        try {
            ServletInputStream inputStream = request.getInputStream();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;

            ;
            while ((len = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, len);
            }

            outputStream.close();
            inputStream.close();

            String result = new String(outputStream.toByteArray(), "UTF-8");

            //将xml 转换成 map

            Map<String, String> stringMap = WXPayUtil.xmlToMap(result);

            String attached = stringMap.get("attach");
            Map<String, String> paramters = JSON.parseObject(attached, Map.class);

            System.out.println("attached::" + paramters);//{"username":"szitheima","outtradeno":"1132530879663575559","money":"1","queue":"queue.seckillorder"}

            //动态获取 交换机 和 routingkey     //exchange.seckillorder  queue.seckillorder
            String exchange = paramters.get("exchange");

            String routingkey = paramters.get("routingkey");

            if (StringUtils.isEmpty(exchange)) {
                exchange = "exchange.seckillorder";
            }
            if (StringUtils.isEmpty(routingkey)) {
                routingkey = "queue.seckillorder";
            }

            this.rabbitTemplate.convertAndSend(exchange, routingkey, JSON.toJSONString(stringMap));
            //响应数据
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("return_code", "SUCCESS");
            hashMap.put("return_msg", "OK");

            return WXPayUtil.mapToXml(hashMap);

        } catch (Exception e) {
            e.printStackTrace();
            //错误日志
        }

        return null;

    }
}
