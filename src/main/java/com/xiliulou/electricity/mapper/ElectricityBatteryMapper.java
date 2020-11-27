package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.PageQuery;
import com.xiliulou.electricity.vo.ElectricityBatteryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 换电柜电池表(ElectricityBattery)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
public interface ElectricityBatteryMapper extends BaseMapper<ElectricityBattery> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityBattery queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElectricityBattery> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param ElectricityBattery 实例对象
     * @return 对象列表
     */
    List<ElectricityBattery> queryAll(ElectricityBattery ElectricityBattery);

    /**
     * 新增数据
     *
     * @param ElectricityBattery 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityBattery ElectricityBattery);

    /**
     * 修改数据
     *
     * @param ElectricityBattery 实例对象
     * @return 影响行数
     */
    int update(ElectricityBattery ElectricityBattery);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ElectricityBatteryVo> getElectricityBatteryPage(ElectricityBatteryQuery electricityBatteryQuery, PageQuery pageQuery);
}