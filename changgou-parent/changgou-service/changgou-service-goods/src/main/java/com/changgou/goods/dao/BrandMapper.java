package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:hftang
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {
    @Select("SELECT  * FROM tb_brand WHERE id in (SELECT brand_id FROM tb_category_brand WHERE category_id = #{categoryId})")
    List<Brand> queryBrandByCategoryId(Integer categoryId);
}
