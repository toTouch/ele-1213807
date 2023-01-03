package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.BatteryTrackRecord;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (BatteryTrackRecord)表数据库访问层
 *
 * @author makejava
 * @since 2023-01-03 16:24:37
 */
public interface BatteryTrackRecordMapper extends BaseMapper<BatteryTrackRecord> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BatteryTrackRecord queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<BatteryTrackRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param batteryTrackRecord 实例对象
     * @return 对象列表
     */
    List<BatteryTrackRecord> queryAll(BatteryTrackRecord batteryTrackRecord);
    
    /**
     * 新增数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 影响行数
     */
    int insertOne(BatteryTrackRecord batteryTrackRecord);
    
    /**
     * 修改数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 影响行数
     */
    int update(BatteryTrackRecord batteryTrackRecord);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<BatteryTrackRecord> queryTrackRecordByCondition(@Param("sn") String sn, @Param("size") Integer size,
            @Param("offset") Integer offset, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
