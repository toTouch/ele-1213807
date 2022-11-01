package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ReportManagement;
import com.xiliulou.electricity.query.ReportManagementQuery;

import java.util.List;

/**
 * 报表管理(ReportManagement)表服务接口
 *
 * @author zzlong
 * @since 2022-10-31 15:59:06
 */
public interface ReportManagementService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ReportManagement selectByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ReportManagement selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     * @return 对象列表
     */
    List<ReportManagement> selectByPage(ReportManagementQuery query);

    Integer selectByPageCount(ReportManagementQuery query);

    /**
     * 新增数据
     *
     * @param reportManagement 实例对象
     * @return 实例对象
     */
    ReportManagement insert(ReportManagement reportManagement);

    /**
     * 修改数据
     *
     * @param reportManagement 实例对象
     * @return 实例对象
     */
    Integer update(ReportManagement reportManagement);

    /**
     * 通过主键删除数据
     *
     * @return 是否成功
     */
    Boolean deleteByQuery(ReportManagementQuery query);


}
