package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import com.sun.xml.internal.ws.util.xml.XmlUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/3/8 16:53
 * @Description:
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.partner}")
    private String partner;
    @Value("${weixin.partnerkey}")
    private String partnerKey;
    @Value("${weixin.notifyurl}")
    private String notifyurl;


    @Override
    public Map createNative(Map<String,String> paramter) {

        //1、封装参数
        Map param = new HashMap();
        param.put("appid", appid);                              //应用ID
        param.put("mch_id", partner);                           //商户ID号
        param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机数
        param.put("body", "畅购");                                //订单描述
        param.put("out_trade_no", paramter.get("outtradeno"));                 //商户订单号
        param.put("total_fee", paramter.get("money"));                      //交易金额
        param.put("spbill_create_ip", "127.0.0.1");           //终端IP
        param.put("notify_url", notifyurl);                    //回调地址
        param.put("trade_type", "NATIVE");                     //交易类型
        param.put("attach", JSON.toJSONString(paramter));//把东西传过去


        //2 将参数转换成xml

        try {
            String paramXml = WXPayUtil.generateSignedXml(param, partnerKey);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            String content = httpClient.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);

            System.out.println("===:" + stringMap);

            //5、获取部分页面所需参数
            Map<String, String> dataMap = new HashMap<String, String>();
            dataMap.put("code_url", stringMap.get("code_url"));
            dataMap.put("out_trade_no", paramter.get("outtradeno"));
            dataMap.put("total_fee", paramter.get("money"));

            return dataMap;


        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    /***
     * 查询支付状态
     * @param out_trade_no 订单号
     * @return
     */

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {

        //1.封装参数
        Map param = new HashMap();
        param.put("appid", appid);                            //应用ID
        param.put("mch_id", partner);                         //商户号
        param.put("out_trade_no", out_trade_no);              //商户订单编号
        param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符

        //转换成xml


        try {
            String mapToXml = WXPayUtil.generateSignedXml(param,partnerKey);

            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(mapToXml);
            httpClient.post();
            String content = httpClient.getContent();

            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
