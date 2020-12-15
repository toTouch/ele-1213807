package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.RolePermission;
import java.util.List;

/**
 * (RolePermission)表服务接口
 *
 * @author makejava
 * @since 2020-12-09 14:36:22
 */
public interface RolePermissionService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    RolePermission queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    RolePermission queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<RolePermission> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param rolePermission 实例对象
     * @return 实例对象
     */
    RolePermission insert(RolePermission rolePermission);

    /**
     * 修改数据
     *
     * @param rolePermission 实例对象
     * @return 实例对象
     */
    Integer update(RolePermission rolePermission);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

	List<Long> queryPidsByRid(Long rid);

	boolean deleteByRoleIdAndPermissionId(Long rolePerId, Long permissionId);

    boolean deleteByRoleId(Long roleId);
}