package com.changgou.seckill.task;

import com.alibaba.fastjson.JSONObject;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Auther: hftang
 * @Date: 2020/3/17 10:53
 * @Description: 多线程下单
 */
@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;


    @Async
    public void createOrder() {


        try {

            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();

            if (seckillStatus != null) {


                //防止超卖
                Object goodsObj = redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillStatus.getGoodsId()).rightPop();

                if (goodsObj == null) {
                    //清除当前用户的排队信息
                    clearQueue(seckillStatus);
                    return;
                }

                //时间区间
                String time = seckillStatus.getTime();
                //用户登录名
                String username = seckillStatus.getUsername();
                //用户抢购商品
                Long id = seckillStatus.getGoodsId();

                //根据id 获取秒杀的数据
                //获取商品数据
                SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);


                //如果没有库存，则直接抛出异常
                if (goods == null || goods.getStockCount() <= 0) {
                    throw new RuntimeException("已售罄!");
                }
                //4.创建一个预订单
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(idWorker.nextId());//订单的ID
                seckillOrder.setSeckillId(id);//秒杀商品ID
                seckillOrder.setMoney(goods.getCostPrice());//金额
                seckillOrder.setUserId(username);//登录的用户名
                seckillOrder.setCreateTime(new Date());//创建时间
                seckillOrder.setStatus("0");//未支付

                //将秒杀订单存入到Redis中
                redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

                //库存减少
//                goods.setStockCount(goods.getStockCount() - 1);

                Long goodsCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1);
                goods.setStockCount(goodsCount.intValue());

                //判断当前商品是否还有库存
                if (goods.getStockCount() <= 0) {
                    //并且将商品数据同步到MySQL中
                    seckillGoodsMapper.updateByPrimaryKeySelective(goods);
                    //如果没有库存,则清空Redis缓存中该商品
                    redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
                } else {
                    //如果有库存，则直数据重置到Reids中
                    redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, goods);
                }


                //创建订单成功了 修改用户的抢单的信息
                seckillStatus.setOrderId(seckillOrder.getId());
                seckillStatus.setStatus(2);//
                seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));
                //重新设置回redis中 UserQueueStatus
                redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            System.out.println(message);
        }


    }

    /**
     * 清理用户排队信息
     *
     * @param seckillStatus
     */

    private void clearQueue(SeckillStatus seckillStatus) {
        //清除排队标示
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());

        //清除抢单标示
        redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());
    }
}
