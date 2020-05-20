package com.changgou.order.service;

import entity.Result;
import pojo.OrderItem;

import java.util.List;

/**
 * @Auther: hftang
 * @Date: 2020/3/3 10:02
 * @Description:
 */
public interface CartService {


    /***
     * 添加购物车
     * @param num:购买商品数量
     * @param id：购买ID
     * @param username：购买用户
     * @return
     */

    void add(Integer num, Long id, String username);

    /**
     * 查询购物车
     * 根据用户名来查
     */

    List<OrderItem> list(String username);
}
