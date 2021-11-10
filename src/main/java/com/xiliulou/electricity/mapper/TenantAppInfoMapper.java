package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.TenantAppInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (TenantAppInfo)表数据库访问层
 *
 * @author makejava
 * @since 2021-07-21 09:57:44
 */
public interface TenantAppInfoMapper extends BaseMapper<TenantAppInfo> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TenantAppInfo queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<TenantAppInfo> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param tenantAppInfo 实例对象
     * @return 对象列表
     */
    List<TenantAppInfo> queryAll(TenantAppInfo tenantAppInfo);

    /**
     * 新增数据
     *
     * @param tenantAppInfo 实例对象
     * @return 影响行数
     */
    int insertOne(TenantAppInfo tenantAppInfo);

    /**
     * 修改数据
     *
     * @param tenantAppInfo 实例对象
     * @return 影响行数
     */
    int update(TenantAppInfo tenantAppInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    TenantAppInfo queryByTenantIdAndType(@Param("tenantId") Integer tenantId, @Param("appType") String appType);

    TenantAppInfo queryByAppId(@Param("appId") String appId, @Param("appType") String mtType);
}
