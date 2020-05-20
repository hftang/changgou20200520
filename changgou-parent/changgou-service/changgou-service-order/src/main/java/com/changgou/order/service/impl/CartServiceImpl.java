package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import pojo.OrderItem;

import java.util.List;

/**
 * @Auther: hftang
 * @Date: 2020/3/3 10:07
 * @Description:
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisTemplate redisTemplate;


    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;


    /***
     * 加入购物车
     * @param num:购买商品数量
     * @param id：购买ID
     * @param username：购买用户
     * @return
     */

    @Override
    public void add(Integer num, Long id, String username) {

        //如果num<=0 则删除
        if (num <= 0) {
            this.redisTemplate.boundHashOps("cart_" + username).delete(id);
            return;
        }

        Result<Sku> skuResult = this.skuFeign.findById(id);

        if (skuResult != null && skuResult.isFlag()) {
            Sku sku = skuResult.getData();

            Result<Spu> spuResult = this.spuFeign.findById(sku.getSpuId());

            //将sku转换成OrderItem

            OrderItem orderItem = sku2OrderItem(sku, spuResult.getData(), num);

            //存入redis
            this.redisTemplate.boundHashOps("cart_" + username).put(id, orderItem);

        }

    }

    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> orderItems = this.redisTemplate.boundHashOps("cart_" + username).values();
        return orderItems;
    }

    private OrderItem sku2OrderItem(Sku sku, Spu data, Integer num) {

        OrderItem orderItem = new OrderItem();

        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setPrice(sku.getPrice());

        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setPayMoney(num * orderItem.getPrice());
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(num * sku.getWeight());

        //分类id

        orderItem.setCategoryId1(data.getCategory1Id());
        orderItem.setCategoryId2(data.getCategory2Id());
        orderItem.setCategoryId3(data.getCategory3Id());


        return orderItem;
    }
}
