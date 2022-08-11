package com.xiliulou.electricity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityCabinetHysicsOperRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (ElectricityCabinetHysicsOperRecord)表数据库访问层
 *
 * @author Hardy
 * @since 2022-08-08 14:42:06
 */
public interface ElectricityCabinetHysicsOperRecordMapper extends BaseMapper<ElectricityCabinetHysicsOperRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetHysicsOperRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElectricityCabinetHysicsOperRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param electricityCabinetHysicsOperRecord 实例对象
     * @return 对象列表
     */
    List<ElectricityCabinetHysicsOperRecord> queryAll(ElectricityCabinetHysicsOperRecord electricityCabinetHysicsOperRecord);

    /**
     * 新增数据
     *
     * @param electricityCabinetHysicsOperRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ElectricityCabinetHysicsOperRecord electricityCabinetHysicsOperRecord);

    /**
     * 修改数据
     *
     * @param electricityCabinetHysicsOperRecord 实例对象
     * @return 影响行数
     */
    int update(ElectricityCabinetHysicsOperRecord electricityCabinetHysicsOperRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ElectricityCabinetHysicsOperRecord> cupboardOperRecordList(@Param("size") Integer size,
                                                                    @Param("offset") Integer offset,
                                                                    @Param("eleIdList") List<Integer> eleIdList,
                                                                    @Param("type") Integer type,
                                                                    @Param("beginTime") Long beginTime,
                                                                    @Param("endTime") Long endTime,
                                                                    @Param("cellNo") Integer cellNo);
}
