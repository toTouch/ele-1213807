package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.City;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (City)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-25 16:20:18
 */
public interface CityMapper  extends BaseMapper<City>{

    /**
     * 通过ID查询单条数据
     *
     * @param cid 主键
     * @return 实例对象
     */
    City queryById(Integer cid);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param city 实例对象
     * @return 对象列表
     */
    List<City> queryAll(City city);


}