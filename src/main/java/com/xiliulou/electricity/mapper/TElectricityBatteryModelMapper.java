package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.TElectricityBatteryModel;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 电池型号(TElectricityBatteryModel)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
public interface TElectricityBatteryModelMapper  extends BaseMapper<TElectricityBatteryModel>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TElectricityBatteryModel queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<TElectricityBatteryModel> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param tElectricityBatteryModel 实例对象
     * @return 对象列表
     */
    List<TElectricityBatteryModel> queryAll(TElectricityBatteryModel tElectricityBatteryModel);

    /**
     * 新增数据
     *
     * @param tElectricityBatteryModel 实例对象
     * @return 影响行数
     */
    int insertOne(TElectricityBatteryModel tElectricityBatteryModel);

    /**
     * 修改数据
     *
     * @param tElectricityBatteryModel 实例对象
     * @return 影响行数
     */
    int update(TElectricityBatteryModel tElectricityBatteryModel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}