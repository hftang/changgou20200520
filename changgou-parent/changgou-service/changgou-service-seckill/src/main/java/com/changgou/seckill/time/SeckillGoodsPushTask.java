package com.changgou.seckill.time;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Auther: hftang
 * @Date: 2020/3/13 18:11
 * @Description: 定时任务
 */

@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis() {

        System.out.println("------>定时开始");

        //获取时间段集合
        List<Date> dateMenus = DateUtil.getDateMenus();

        for (Date startTime : dateMenus) {

            String extName = DateUtil.data2str(startTime, DateUtil.PATTERN_YYYYMMDDHH);

            //根据时间段来查询符合条件的数据

            Example example = new Example(SeckillGoods.class);

            Example.Criteria criteria = example.createCriteria();
            //通过审核的
            criteria.andEqualTo("status", "1");
            //库存大于0的
            criteria.andGreaterThan("stockCount", 0);
            //开始时间《=活动开始时间
            criteria.andLessThanOrEqualTo("startTime", startTime);
            //结束时间 《 开始时间+2
            criteria.andLessThan("endTime", DateUtil.addDateHour(startTime, 2));
            //排除已存在redis中存在的商品

            Set keys = this.redisTemplate.boundHashOps("SeckillGoods_" + extName).keys();

            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }

            List<SeckillGoods> seckillGoods = this.seckillGoodsMapper.selectByExample(example);

            //将秒杀的商品存入redis中

            for (SeckillGoods seckillGood : seckillGoods) {
                System.out.println("---存入秒杀goods:" + seckillGood.getName() + ":" + seckillGood.getStartTime() + ":" + seckillGood.getEndTime());

                //为了防止超卖
                long[] pushIds = pushIds(seckillGood.getStockCount(), seckillGood.getId());

                System.out.println("pushIds:::->" + Arrays.toString(pushIds));

                for (int i = 0; i < pushIds.length; i++) {

                    //存入队列
                    this.redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGood.getId()).leftPush(pushIds[i]);


                }


                //自增计数器
                this.redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGood.getId(), seckillGood.getStockCount());

                //存
                this.redisTemplate.boundHashOps("SeckillGoods_" + extName).put(seckillGood.getId(), seckillGood);
                //设置过期时间
                this.redisTemplate.expireAt("SeckillGoods_" + extName, DateUtil.addDateHour(startTime, 2));

            }

        }


    }

    /**
     * 向一个数组中添加商品的id
     *
     * @param len
     * @param id
     * @return
     */

    private long[] pushIds(int len, Long id) {
        long[] ids = new long[len];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
