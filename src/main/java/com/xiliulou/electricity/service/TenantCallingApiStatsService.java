package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.TenantCallingApiStats;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:21
 * @Description:
 */
public interface TenantCallingApiStatsService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    TenantCallingApiStats queryByTenantIdFromDB(Integer id);

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    TenantCallingApiStats queryByIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<TenantCallingApiStats> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param tenantCallingApiStats 实例对象
     * @return 实例对象
     */
    TenantCallingApiStats insert(TenantCallingApiStats tenantCallingApiStats);

    /**
     * 修改数据
     *
     * @param tenantCallingApiStats 实例对象
     * @return 实例对象
     */
    Integer update(TenantCallingApiStats tenantCallingApiStats);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

}
