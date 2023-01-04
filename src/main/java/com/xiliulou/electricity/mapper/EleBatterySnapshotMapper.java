package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleBatterySnapshot;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (EleBatterySnapshot)表数据库访问层
 *
 * @author makejava
 * @since 2023-01-04 09:21:26
 */
public interface EleBatterySnapshotMapper extends BaseMapper<EleBatterySnapshot> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleBatterySnapshot queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleBatterySnapshot> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleBatterySnapshot 实例对象
     * @return 对象列表
     */
    List<EleBatterySnapshot> queryAll(EleBatterySnapshot eleBatterySnapshot);
    
    /**
     * 新增数据
     *
     * @param eleBatterySnapshot 实例对象
     * @return 影响行数
     */
    int insertOne(EleBatterySnapshot eleBatterySnapshot);
    
    /**
     * 修改数据
     *
     * @param eleBatterySnapshot 实例对象
     * @return 影响行数
     */
    int update(EleBatterySnapshot eleBatterySnapshot);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<EleBatterySnapshot> queryBatterySnapshot(@Param("eId") Integer eId, @Param("size") Integer size,
            @Param("offset") Integer offset,@Param("startTime") Long startTime,@Param("endTime") Long endTime);
}
