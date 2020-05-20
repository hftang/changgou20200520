package com.changgou.pay.service;

import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/3/8 16:51
 * @Description:
 */
public interface WeixinPayService {

    /**
     * 生成二维码
     *
     * @param out_trade_no 客户端自定义订单编号
     * @param total_fee    交易金额,单位：分
     * @return
     */

    public Map createNative(Map<String,String> paramter);

    /***
     * 查询支付状态
     * @param out_trade_no 订单号
     * @return
     */
    public Map<String,String > queryPayStatus(String out_trade_no);

}
