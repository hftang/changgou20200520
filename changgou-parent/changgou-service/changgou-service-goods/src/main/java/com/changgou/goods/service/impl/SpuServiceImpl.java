package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:hftang
 * @Description:Spu业务层接口实现类
 * @Date 2019/12/26
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;

    @Override
    public int putMany(Long[] ids) {


        Spu spu = new Spu();
        spu.setIsMarketable("1");
        Example example = new Example(Spu.class);

        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable", 0);
        criteria.andEqualTo("status", 1);
        criteria.andEqualTo("isDelete", 0);


        int i = this.spuMapper.updateByExampleSelective(spu, example);

        return i;
    }

    @Override
    public void logicDelete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否下架的商品
        if(!spu.getIsMarketable().equals("0")){
            throw new RuntimeException("必须先下架再删除！");
        }
        //删除
        spu.setIsDelete("1");
        //未审核
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 恢复数据
     * @param spuId
     */
    @Override
    public void restore(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否删除的商品
        if(!spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品未删除！");
        }
        //未删除
        spu.setIsDelete("0");
        //未审核
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 根据spuid查询goods
     * @param id
     * @return
     */

    @Override
    public Goods findGoodsBySpuId(Integer id) {
        //因为Goods 用的 spu 和 sku
        Spu spu = this.spuMapper.selectByPrimaryKey(id);

        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", id);
        List<Sku> skuList = this.skuMapper.selectByExample(example);

        Goods goods = new Goods();

        goods.setSpu(spu);
        goods.setSkuList(skuList);


        return goods;
    }

    @Override
    public Goods findGoodsById(Long spuId) {
        //查询Spu
        Spu spu = spuMapper.selectByPrimaryKey(spuId);

        //查询List<Sku>
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        //封装Goods
        Goods goods = new Goods();
        goods.setSkuList(skus);
        goods.setSpu(spu);

        return goods;
    }

    /**
     * 根据spuid 审核
     *
     * @param id
     */

    @Override
    public void auditSpuBySpuId(String id) {

        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        if (spu.getIsDelete().equalsIgnoreCase("1")) {
            throw new RuntimeException("该商品已被删除");
        }

        spu.setStatus("1");
        spu.setIsMarketable("1");

        this.spuMapper.updateByPrimaryKeySelective(spu);

    }

    /**
     * 商品下架
     *
     * @param spuId
     */

    @Override
    public void pull(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        spu.setIsMarketable("0");//下架状态
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 商品上架
     * @param spuId
     */
    @Override
    public void put(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查是否删除的商品
        if (spu.getIsDelete().equals("1")) {
            throw new RuntimeException("此商品已删除！");
        }
        if (!spu.getStatus().equals("1")) {
            throw new RuntimeException("未通过审核的商品不能！");
        }
        //上架状态
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /***
     * 添加商品
     * @param goods
     */

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addGoods(Goods goods) {

        /**
         * 因为是两张表 spu 和 sku 表
         * 所以要分别添加
         */
        if (goods == null) {
            return;
        }
        long goodId = idWorker.nextId();

        Spu spu = goods.getSpu();

        if (spu.getId() == null) {
            //添加
            spu.setId(goodId);
            this.spuMapper.insertSelective(spu);
        } else {
            //修改
            this.spuMapper.updateByPrimaryKeySelective(spu);
            //删除之前的
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }


        List<Sku> skuList = goods.getSkuList();


        Date date = new Date();//当前日期
        //获取分类
        Integer category3Id = spu.getCategory3Id();

        Category category = this.categoryMapper.selectByPrimaryKey(category3Id);
        //分类名称
        String categoryName = category.getName();

        //品牌名称
        Integer brandId = spu.getBrandId();
        Brand brand = this.brandMapper.selectByPrimaryKey(brandId);
        String brandName = brand.getName();

        if (skuList != null) {
            for (Sku sku : skuList) {
                //设置主键
                sku.setId(idWorker.nextId());
                //设置spec规格
                if (StringUtils.isEmpty(sku.getSpec())) {
                    sku.setSpec("{}");
                }
                //设置sku的名字 = spu.name + spec

                String sku_name = spu.getName();

                Map<String, String> specMap = JSON.parseObject(sku.getSpec(), Map.class);

                if (specMap != null && specMap.size() > 0) {

                    for (String value : specMap.values()) {
                        sku_name += "" + value;
                    }
                }

                sku.setName(sku_name);
                sku.setSpuId(spu.getId());
                sku.setCreateTime(date);
                sku.setUpdateTime(date);
                sku.setCategoryId(category.getId());
                sku.setCategoryName(categoryName);
                sku.setBrandName(brandName);

                skuMapper.insertSelective(sku);


            }

        }


    }


    /**
     * Spu条件+分页查询
     *
     * @param spu  查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     *
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu) {
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     *
     * @param spu
     * @return
     */
    public Example createExample(Spu spu) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (spu != null) {
            // 主键
            if (!StringUtils.isEmpty(spu.getId())) {
                criteria.andEqualTo("id", spu.getId());
            }
            // 货号
            if (!StringUtils.isEmpty(spu.getSn())) {
                criteria.andEqualTo("sn", spu.getSn());
            }
            // SPU名
            if (!StringUtils.isEmpty(spu.getName())) {
                criteria.andLike("name", "%" + spu.getName() + "%");
            }
            // 副标题
            if (!StringUtils.isEmpty(spu.getCaption())) {
                criteria.andEqualTo("caption", spu.getCaption());
            }
            // 品牌ID
            if (!StringUtils.isEmpty(spu.getBrandId())) {
                criteria.andEqualTo("brandId", spu.getBrandId());
            }
            // 一级分类
            if (!StringUtils.isEmpty(spu.getCategory1Id())) {
                criteria.andEqualTo("category1Id", spu.getCategory1Id());
            }
            // 二级分类
            if (!StringUtils.isEmpty(spu.getCategory2Id())) {
                criteria.andEqualTo("category2Id", spu.getCategory2Id());
            }
            // 三级分类
            if (!StringUtils.isEmpty(spu.getCategory3Id())) {
                criteria.andEqualTo("category3Id", spu.getCategory3Id());
            }
            // 模板ID
            if (!StringUtils.isEmpty(spu.getTemplateId())) {
                criteria.andEqualTo("templateId", spu.getTemplateId());
            }
            // 运费模板id
            if (!StringUtils.isEmpty(spu.getFreightId())) {
                criteria.andEqualTo("freightId", spu.getFreightId());
            }
            // 图片
            if (!StringUtils.isEmpty(spu.getImage())) {
                criteria.andEqualTo("image", spu.getImage());
            }
            // 图片列表
            if (!StringUtils.isEmpty(spu.getImages())) {
                criteria.andEqualTo("images", spu.getImages());
            }
            // 售后服务
            if (!StringUtils.isEmpty(spu.getSaleService())) {
                criteria.andEqualTo("saleService", spu.getSaleService());
            }
            // 介绍
            if (!StringUtils.isEmpty(spu.getIntroduction())) {
                criteria.andEqualTo("introduction", spu.getIntroduction());
            }
            // 规格列表
            if (!StringUtils.isEmpty(spu.getSpecItems())) {
                criteria.andEqualTo("specItems", spu.getSpecItems());
            }
            // 参数列表
            if (!StringUtils.isEmpty(spu.getParaItems())) {
                criteria.andEqualTo("paraItems", spu.getParaItems());
            }
            // 销量
            if (!StringUtils.isEmpty(spu.getSaleNum())) {
                criteria.andEqualTo("saleNum", spu.getSaleNum());
            }
            // 评论数
            if (!StringUtils.isEmpty(spu.getCommentNum())) {
                criteria.andEqualTo("commentNum", spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if (!StringUtils.isEmpty(spu.getIsMarketable())) {
                criteria.andEqualTo("isMarketable", spu.getIsMarketable());
            }
            // 是否启用规格
            if (!StringUtils.isEmpty(spu.getIsEnableSpec())) {
                criteria.andEqualTo("isEnableSpec", spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if (!StringUtils.isEmpty(spu.getIsDelete())) {
                criteria.andEqualTo("isDelete", spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if (!StringUtils.isEmpty(spu.getStatus())) {
                criteria.andEqualTo("status", spu.getStatus());
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
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     *
     * @param spu
     */
    @Override
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     *
     * @param spu
     */
    @Override
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     *
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }


}
