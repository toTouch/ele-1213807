package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleChargeConfig;
import java.util.List;

import com.xiliulou.electricity.query.ChargeConfigListQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (EleChargeConfig)表数据库访问层
 *
 * @author makejava
 * @since 2023-07-18 10:21:40
 */
public interface EleChargeConfigMapper  extends BaseMapper<EleChargeConfig>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleChargeConfig queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<EleChargeConfig> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleChargeConfig 实例对象
     * @return 对象列表
     */
    List<EleChargeConfig> queryAll(EleChargeConfig eleChargeConfig);

    /**
     * 新增数据
     *
     * @param eleChargeConfig 实例对象
     * @return 影响行数
     */
    int insertOne(EleChargeConfig eleChargeConfig);

    /**
     * 修改数据
     *
     * @param eleChargeConfig 实例对象
     * @return 影响行数
     */
    int update(EleChargeConfig eleChargeConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<EleChargeConfig> queryList(ChargeConfigListQuery chargeConfigListQuery);

}
