package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElePower;
import com.xiliulou.electricity.query.ElePowerListQuery;
import com.xiliulou.electricity.vo.ElePowerDayDetailVo;
import com.xiliulou.electricity.vo.ElePowerDayVo;
import com.xiliulou.electricity.vo.ElePowerMonthDetailVo;
import com.xiliulou.electricity.vo.ElePowerMothVo;
import com.xiliulou.electricity.vo.EleSumPowerVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (ElePower)表数据库访问层
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
public interface ElePowerMapper extends BaseMapper<ElePower> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElePower queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ElePower> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param elePower 实例对象
     * @return 对象列表
     */
    List<ElePower> queryAll(ElePower elePower);

    /**
     * 新增数据
     *
     * @param elePower 实例对象
     * @return 影响行数
     */
    int insertOne(ElePower elePower);

    /**
     * 修改数据
     *
     * @param elePower 实例对象
     * @return 影响行数
     */
    int update(ElePower elePower);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int insertOrUpdate(ElePower power);

    List<ElePower> queryPartAttList(ElePowerListQuery query);

    List<ElePowerDayVo> queryDayList(@Param("eid") Long eid, @Param("startTime") Long startTime,
                                     @Param("endTime") Long endTime, @Param("tenantId") Integer tenantId);

    List<ElePowerMothVo> queryMonthList(@Param("eid") Long eid, @Param("startTime") Long startTime,
                                        @Param("endTime") Long endTime, @Param("tenantId") Integer tenantId);

    List<ElePowerDayDetailVo> queryDayDetail(@Param("eid") Long eid, @Param("startTime") Long startTime,
                                             @Param("endTime") Long endTime, @Param("tenantId") Integer tenantId);

    List<ElePowerMonthDetailVo> queryMonthDetail(@Param("eid") Long eid, @Param("startTime") Long startTime,
                                                          @Param("endTime") Long endTime, @Param("tenantId") Integer tenantId);

//    void queryDayList(Long eid, Long startTime, Long endTime, Integer tenantId);
    
    EleSumPowerVO selectListByCondition(@Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("eidList") List<Long> eidList, @Param("tenantId") Integer tenantId);
    
    ElePower selectLatestByEid(@Param("eid") Long eid, @Param("reportTime") Long reportTime);
    
    List<ElePower> selectListByEids(@Param("eIdList") List<Integer> electricityCabinetIdList);
    
    Integer exitsByEidAndReportTime(@Param("eid") Long eid, @Param("reportTime") Long reportTime);
    
    //    void queryDayList(Long eid, Long startTime, Long endTime, Integer tenantId);
}
