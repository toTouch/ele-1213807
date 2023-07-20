package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElePowerMonthRecord;
import java.util.List;

import com.xiliulou.electricity.query.PowerMonthStatisticsQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ElePowerMonthRecord)表数据库访问层
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
public interface ElePowerMonthRecordMapper  extends BaseMapper<ElePowerMonthRecord>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ElePowerMonthRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ElePowerMonthRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param elePowerMonthRecord 实例对象
     * @return 对象列表
     */
    List<ElePowerMonthRecord> queryAll(ElePowerMonthRecord elePowerMonthRecord);

    /**
     * 新增数据
     *
     * @param elePowerMonthRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ElePowerMonthRecord elePowerMonthRecord);

    /**
     * 修改数据
     *
     * @param elePowerMonthRecord 实例对象
     * @return 影响行数
     */
    int update(ElePowerMonthRecord elePowerMonthRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ElePowerMonthRecord> queryPartAttrList(PowerMonthStatisticsQuery query);

    Integer queryCount(PowerMonthStatisticsQuery query);

}
