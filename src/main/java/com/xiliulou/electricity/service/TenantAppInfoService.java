package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.TenantAppInfo;
import com.xiliulou.electricity.web.query.AppInfoQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (TenantAppInfo)表服务接口
 *
 * @author makejava
 * @since 2021-07-21 09:57:45
 */
public interface TenantAppInfoService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    TenantAppInfo queryByIdFromDB(Integer id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    TenantAppInfo queryByIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<TenantAppInfo> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param tenantAppInfo 实例对象
     * @return 实例对象
     */
    TenantAppInfo insert(TenantAppInfo tenantAppInfo);

    /**
     * 修改数据
     *
     * @param tenantAppInfo 实例对象
     * @return 实例对象
     */
    Integer update(TenantAppInfo tenantAppInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

    Triple<Boolean, String, Object> saveApp(AppInfoQuery appInfoQuery);

    String generateAppCacheKey(Integer tenantId, String appType);

    String generateAppCacheKey(String appId, String appType);

    TenantAppInfo queryByTenantIdAndAppType(Integer tenantId, String appType);

    Triple<Boolean, String, Object> queryAppInfo(String appType);

    TenantAppInfo queryByAppId(String appId, String mtType);
}
