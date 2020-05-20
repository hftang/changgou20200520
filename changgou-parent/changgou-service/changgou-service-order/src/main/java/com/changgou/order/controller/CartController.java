package com.changgou.order.controller;

import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.OrderItem;

import java.util.List;

/**
 * @Auther: hftang
 * @Date: 2020/3/3 11:27
 * @Description:
 */
@RestController
@RequestMapping("/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private TokenDecode tokenDecode;

    /***
     *
     * @param num  数量
     * @param id  skuid
     * @return
     */

    @RequestMapping(value = "/add")
    public Result add(Integer num, Long id) {
        //用户名
//        String username = "itheima";
        String username = this.tokenDecode.getUserInfo().get("username");


        this.cartService.add(num, id, username);
        return new Result(true, StatusCode.OK, "加入购物车成功！");
    }

    /**
     * 根据用户名 查询购物车
     */

    @GetMapping(value = "/list")
    public Result list() {

        //从token中获取用户名称
        String username = this.tokenDecode.getUserInfo().get("username");

//        String username = "itheima";
        List<OrderItem> list = this.cartService.list(username);
        return new Result(true, StatusCode.OK, "购物车列表查询成功！", list);
    }
}
