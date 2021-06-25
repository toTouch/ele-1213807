package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.RolePermission;

import java.util.ArrayList;
import java.util.List;

/**
 * (RolePermission)表服务接口
 *
 * @author makejava
 * @since 2020-12-09 14:36:22
 */
public interface RolePermissionService {



    /**
     * 新增数据
     *
     * @param rolePermission 实例对象
     * @return 实例对象
     */
    RolePermission insert(RolePermission rolePermission);


	List<Long> queryPidsByRid(Long rid);


    boolean deleteByRoleId(Long roleId);

}
