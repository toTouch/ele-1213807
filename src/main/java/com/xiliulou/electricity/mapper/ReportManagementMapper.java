package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ReportManagement;
import com.xiliulou.electricity.query.ReportManagementQuery;

import java.util.List;

/**
 * 报表管理(ReportManagement)表数据库访问层
 *
 * @author zzlong
 * @since 2022-10-31 15:59:06
 */
public interface ReportManagementMapper  extends BaseMapper<ReportManagement>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ReportManagement selectById(Long id);

    /**
     * 查询指定行数据
     * @return 对象列表
     */
    List<ReportManagement> selectByPage(ReportManagementQuery query);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param reportManagement 实例对象
     * @return 对象列表
     */
    List<ReportManagement> selectByQuery(ReportManagement reportManagement);

    /**
     * 新增数据
     *
     * @param reportManagement 实例对象
     * @return 影响行数
     */
    int insertOne(ReportManagement reportManagement);

    /**
     * 修改数据
     *
     * @param reportManagement 实例对象
     * @return 影响行数
     */
    int update(ReportManagement reportManagement);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int deleteByQuery(ReportManagementQuery query);

    Integer selectByPageCount(ReportManagementQuery query);
}
