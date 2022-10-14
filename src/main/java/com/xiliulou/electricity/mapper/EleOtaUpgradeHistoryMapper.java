package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.EleOtaUpgradeHistory;
import com.xiliulou.electricity.vo.EleOtaUpgradeHistoryVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (EleOtaUpgradeHistory)表数据库访问层
 *
 * @author Hardy
 * @since 2022-10-14 14:35:39
 */
public interface EleOtaUpgradeHistoryMapper extends BaseMapper<EleOtaUpgradeHistory> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleOtaUpgradeHistory queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<EleOtaUpgradeHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 对象列表
     */
    List<EleOtaUpgradeHistory> queryAll(EleOtaUpgradeHistory eleOtaUpgradeHistory);
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 影响行数
     */
    int insertOne(EleOtaUpgradeHistory eleOtaUpgradeHistory);
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgradeHistory 实例对象
     * @return 影响行数
     */
    int update(EleOtaUpgradeHistory eleOtaUpgradeHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    EleOtaUpgradeHistory queryByUpgradeNo(String upgradeNo);
    
    List<EleOtaUpgradeHistoryVo> queryList(@Param("eid") Integer eid, @Param("cellNo") Integer cellNo,
            @Param("type") Integer type, @Param("upgradeVersion") String upgradeVersion,
            @Param("historyVersion") String historyVersion, @Param("status") String status,
            @Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("offset") Long offset,
            @Param("size") Long size);
    
    Long queryCount(@Param("eid") Integer eid, @Param("cellNo") Integer cellNo, @Param("type") Integer type,
            @Param("upgradeVersion") String upgradeVersion, @Param("historyVersion") String historyVersion,
            @Param("status") String status, @Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
