package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryMaterial;

import java.util.List;

import com.xiliulou.electricity.query.BatteryMaterialQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 电池材质(BatteryMaterial)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-11 10:56:47
 */
public interface BatteryMaterialMapper extends BaseMapper<BatteryMaterial> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryMaterial queryById(Long id);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryMaterial 实例对象
     * @return 对象列表
     */
    List<BatteryMaterial> queryAll(BatteryMaterial batteryMaterial);

    /**
     * 新增数据
     *
     * @param batteryMaterial 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryMaterial batteryMaterial);

    /**
     * 修改数据
     *
     * @param batteryMaterial 实例对象
     * @return 影响行数
     */
    int update(BatteryMaterial batteryMaterial);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<BatteryMaterial> selectByPage(BatteryMaterialQuery query);

    Integer selectByPageCount(BatteryMaterialQuery query);

    Integer checkExistByName(@Param("name") String name);
}
