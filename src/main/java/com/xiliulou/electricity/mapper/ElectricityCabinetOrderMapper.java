package com.xiliulou.electricity.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 订单表(TElectricityCabinetOrder)表数据库访问层
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderMapper extends BaseMapper<ElectricityCabinetOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetOrder> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetOrder 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetOrder> queryAll(ElectricityCabinetOrder electricityCabinetOrder);

    /**
     * 新增数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetOrder electricityCabinetOrder);

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetOrder electricityCabinetOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}