package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Tenant;
import java.util.List;

/**
 * 租户表(Tenant)表服务接口
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
public interface TenantService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Tenant queryByIdFromDB(Integer id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    Tenant queryByIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Tenant> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param tenant 实例对象
     * @return 实例对象
     */
    Tenant insert(Tenant tenant);

    /**
     * 修改数据
     *
     * @param tenant 实例对象
     * @return 实例对象
     */
    Integer update(Tenant tenant);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

}