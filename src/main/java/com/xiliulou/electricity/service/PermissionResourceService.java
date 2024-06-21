package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.web.query.PermissionResourceQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (PermissionResource)表服务接口
 *
 * @author makejava
 * @since 2020-12-09 15:38:23
 */
public interface PermissionResourceService {


	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	PermissionResource queryByIdFromCache(Long id);


	/**
	 * 新增数据
	 *
	 * @param permissionResource 实例对象
	 * @return 实例对象
	 */
	PermissionResource insert(PermissionResource permissionResource);

	/**
	 * 修改数据
	 *
	 * @param permissionResource 实例对象
	 * @return 实例对象
	 */
	Integer update(PermissionResource permissionResource);

	/**
	 * 通过主键删除数据
	 *
	 * @param id 主键
	 * @return 是否成功
	 */
	Boolean deleteById(Long id);

	Pair<Boolean, Object> addPermissionResource(PermissionResourceQuery permissionResourceQuery);

	Pair<Boolean, Object> updatePermissionResource(PermissionResourceQuery permissionResourceQuery);

	Pair<Boolean, Object> deletePermission(Long permissionId);

	Pair<Boolean, Object> bindPermissionToRole(Long roleId, List<Long> pid);


	Pair<Boolean, Object> getList();

	Pair<Boolean, Object> getPermissionsByRole(Long rid);

	List<PermissionResource> queryPermissionsByRole(Long rid);

	List<PermissionResource> queryPermissionsByRoleList(List<Long> rids);

    Pair<Boolean, Object> getPermissionTempleteList();

	/**
	 * 通过角色Id和权限类型查询权限
	 *
	 * @param rids 角色id type 权限类型
	 * @return 实例对象
	 */
	List<PermissionResource> queryByRoleIds(List<Long> rids,Integer type);
}
