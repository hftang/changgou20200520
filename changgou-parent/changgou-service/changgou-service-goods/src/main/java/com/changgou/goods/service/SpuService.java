package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:Spu业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SpuService {

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();

    /**
     * 添加商品
     * @param goods
     */
    void addGoods(Goods goods);

    /***
     * 根据spuid 查询 商品
     * @param id
     * @return
     */
    Goods findGoodsBySpuId(Integer id);

    Goods findGoodsById(Long id);

    /**
     * 根据spuid 审核
     * @param id
     */
    void auditSpuBySpuId(String id);

    void pull(Long id);

    void put(Long id);

    int putMany(Long[] ids);

    void logicDelete(Long id);

    void restore(Long id);
}
