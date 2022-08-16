package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.ElectricityCabinetPhysicsOperRecord;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ElectricityCabinetPhysicsOperRecord)表数据库访问层
 *
 * @author Hardy
 * @since 2022-08-16 15:31:12
 */
public interface ElectricityCabinetPhysicsOperRecordMapper  extends BaseMapper<ElectricityCabinetPhysicsOperRecord>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetPhysicsOperRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetPhysicsOperRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetPhysicsOperRecord> queryAll(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord);

    /**
     * 新增数据
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord);

    /**
     * 修改数据
     *
     * @param electricityCabinetPhysicsOperRecord 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
