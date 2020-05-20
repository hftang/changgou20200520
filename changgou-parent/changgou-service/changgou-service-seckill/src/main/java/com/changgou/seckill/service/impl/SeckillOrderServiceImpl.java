package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/****
 * @Author:hftang
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    /***
     * 关闭订单，回滚库存
     */
    @Override
    public void closeOrder(String username) {

        //将消息转换成SeckillStatus
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        //获取Redis中订单信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        //回滚库存
        //1)从Redis中获取该商品
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).get(seckillStatus.getGoodsId());

        //2)如果Redis中没有，则从数据库中加载
        if(seckillGoods==null){
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
        }

        //3)数量+1  (递增数量+1，队列数量+1)
        Long surplusCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);
        seckillGoods.setStockCount(surplusCount.intValue());
        redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).leftPush(seckillStatus.getGoodsId());

        //4)数据同步到Redis中
        redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).put(seckillStatus.getGoodsId(),seckillGoods);

        //清理排队标示
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());

        //清理抢单标示
        redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());


    }

    /***
     * 更新订单状态
     * @param out_trade_no
     * @param transaction_id
     * @param username
     */
    @Override
    public void updatePayStatus(String out_trade_no, String transaction_id, String username) {
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);

        //修改状态
        seckillOrder.setStatus("1");
        seckillOrder.setPayTime(new Date());

        //同步到mysql
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);

        //删除缓存
        this.redisTemplate.boundHashOps("SeckillOrder").delete(username);

        //删除排队数据
        this.redisTemplate.boundHashOps("UserQueueCount").delete(username);

        //删除抢购的状态

        this.redisTemplate.boundHashOps("UserQueueStatus").delete(username);

    }

    @Override
    public SeckillStatus queryStatus(String username) {
        //seckillStatus
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
//        SeckillStatus seckillStatus = JSON.parseObject(object.toString(), SeckillStatus.class);

        return seckillStatus;
    }

    @Override
    public Boolean add(String time, Long id, String username) {

        try {
            //新增值判断是否已存在
            Long increment = this.redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
            if (increment > 1) {
                //100 表示有重复抢单
                throw new RuntimeException(String.valueOf(StatusCode.REPERROR));
            }

            /**
             * username 抢单的用户是谁
             * status 1  表示抢单的状态 (1.排队中)
             * id 抢的商品的ID
             * time :抢的商品的所属时间段
             */
            SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);
            seckillStatus.setMoney(20.9f);
            seckillStatus.setOrderId(1122121222131333219l);


            //进入排队中
            redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);
            //进入排队标识
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

            this.multiThreadingCreateOrder.createOrder();
        } catch (Exception e) {
            String message = e.getMessage();

            System.out.println(":::" + message);
        }


        //----------------------------------------------------------------------------

//        //获取商品
//        SeckillGoods seckillGoods = (SeckillGoods) this.redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
//
//        if(seckillGoods==null||seckillGoods.getStockCount()<=0){
//            throw new RuntimeException("已售罄!");
//        }
//
//        //如果库存里有货，就创建订单
//        SeckillOrder seckillOrder = new SeckillOrder();
//        seckillOrder.setId(idWorker.nextId());
//        seckillOrder.setSeckillId(id);
//        seckillOrder.setMoney(seckillGoods.getCostPrice());
//        seckillOrder.setUserId(username);
//        seckillOrder.setCreateTime(new Date());
//        seckillOrder.setStatus("0");
//
//        //先把订单存入redis中 等待支付
//        this.redisTemplate.boundHashOps("seckillOrder").put(username,seckillOrder);
//
//        //减库存
//        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
//
//        if(seckillGoods.getStockCount()<=0){
//            //没有库存了
//            //更新数据库
//            this.seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
//
//            //删除缓存
//            this.redisTemplate.boundHashOps("SeckillGoods_"+time).delete(id);
//        }else{
//
//            //库存还有的话，更新下redis
//            this.redisTemplate.boundHashOps("SeckillGoods_"+time).put(id,seckillGoods);
//        }


        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder) {
        Example example = new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                criteria.andEqualTo("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                criteria.andEqualTo("seckillId", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                criteria.andEqualTo("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                criteria.andEqualTo("userId", seckillOrder.getUserId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                criteria.andEqualTo("createTime", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                criteria.andEqualTo("payTime", seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                criteria.andEqualTo("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                criteria.andEqualTo("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                criteria.andEqualTo("transactionId", seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }
}
