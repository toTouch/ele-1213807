package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.web.query.RoleQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (Role)表服务接口
 *
 * @author makejava
 * @since 2020-12-09 14:34:00
 */
public interface RoleService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Role queryByIdFromDB(Long id);


    /**
     * 新增数据
     *
     * @param role 实例对象
     * @return 实例对象
     */
    Role insert(Role role);

    /**
     * 修改数据
     *
     * @param role 实例对象
     * @return 实例对象
     */
    Integer update(Role role);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

	R addRole(RoleQuery roleQuery);

    R updateRole(RoleQuery roleQuery);

    Pair<Boolean, Object> deleteRole(Long id);

    Pair<Boolean, Object> bindUserRole(Long uid, List<Long> roleId);

    Pair<Boolean, Object> getMenuByUid();

    Pair<Boolean, Object> queryBindUidRids(Long uid);

    List<Long> queryRidsByUid(Long uid);

    R queryAll();
}
