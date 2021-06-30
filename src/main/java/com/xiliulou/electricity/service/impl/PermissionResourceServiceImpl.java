package com.xiliulou.electricity.service.impl;

import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.PermissionResourceTree;
import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.entity.RolePermission;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.PermissionResourceMapper;
import com.xiliulou.electricity.service.PermissionResourceService;
import com.xiliulou.electricity.service.RolePermissionService;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.TreeUtils;
import com.xiliulou.electricity.web.query.PermissionResourceQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (PermissionResource)表服务实现类
 *
 * @author makejava
 * @since 2020-12-09 15:38:23
 */
@Service("permissionResourceService")
@Slf4j
public class PermissionResourceServiceImpl implements PermissionResourceService {
	@Resource
	private PermissionResourceMapper permissionResourceMapper;
	@Autowired
	RedisService redisService;
	@Autowired
	RoleService roleService;
	@Autowired
	RolePermissionService rolePermissionService;


	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public PermissionResource queryByIdFromCache(Long id) {

		PermissionResource withHash = redisService.getWithHash(ElectricityCabinetConstant.CACHE_PERMISSION + id, PermissionResource.class);
		if (Objects.nonNull(withHash)) {
			return withHash;
		}

		PermissionResource permissionResource = permissionResourceMapper.queryById(id);
		if (Objects.isNull(permissionResource)) {
			return null;
		}

		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_PERMISSION + id, permissionResource);
		return permissionResource;
	}


	/**
	 * 新增数据
	 *
	 * @param permissionResource 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public PermissionResource insert(PermissionResource permissionResource) {
		this.permissionResourceMapper.insertOne(permissionResource);
		if (Objects.nonNull(permissionResource.getId())) {
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_PERMISSION + permissionResource.getId(), permissionResource);
		}
		return permissionResource;
	}

	/**
	 * 修改数据
	 *
	 * @param permissionResource 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(PermissionResource permissionResource) {
		int update = this.permissionResourceMapper.updateById(permissionResource);
		if (update > 0) {
			redisService.delete(ElectricityCabinetConstant.CACHE_PERMISSION + permissionResource.getId());
		}
		return update;

	}

	/**
	 * 通过主键删除数据
	 *
	 * @param id 主键
	 * @return 是否成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteById(Long id) {
		int i = this.permissionResourceMapper.deleteById(id);
		if (i > 0) {
			redisService.delete(ElectricityCabinetConstant.CACHE_PERMISSION + id);
			return true;
		}
		return false;
	}

	@Override
	public Pair<Boolean, Object> addPermissionResource(PermissionResourceQuery permissionResourceQuery) {

		Long uid = SecurityUtils.getUid();

		PermissionResource permissionResource = new PermissionResource();
		BeanUtils.copyProperties(permissionResourceQuery, permissionResource);

		//检查父元素是否存在
		if (permissionResource.getParent() != 0) {
			PermissionResource parentResource = queryByIdFromCache(permissionResource.getParent());
			if (Objects.isNull(parentResource)) {
				log.error("PERMISSION ERROR! permission no parent!,uid={},parentId={}", uid, permissionResource.getParent());
				return Pair.of(false, "父元素不存在");
			}
		}

		if (Objects.equals(permissionResourceQuery.getType(), PermissionResource.TYPE_URL) && isIllegalMethod(permissionResource.getMethod())) {
			return Pair.of(false, "方法不合法！");
		}

		permissionResource.setCreateTime(System.currentTimeMillis());
		permissionResource.setUpdateTime(System.currentTimeMillis());
		permissionResource.setDelFlag(PermissionResource.DEL_NORMAL);

		PermissionResource insert = insert(permissionResource);

		return Objects.isNull(insert.getId()) ? Pair.of(false, "保存失败") : Pair.of(true, "保存成功");
	}

	private boolean isIllegalMethod(String method) {
		switch (method.toUpperCase()) {
			case "GET":
				return false;
			case "DELETE":
				return false;
			case "PUT":
				return false;
			case "POST":
				return false;
		}
		return true;
	}

	@Override
	public Pair<Boolean, Object> updatePermissionResource(PermissionResourceQuery permissionResourceQuery) {
		Long uid = SecurityUtils.getUid();

		PermissionResource permissionResource = new PermissionResource();
		BeanUtils.copyProperties(permissionResourceQuery, permissionResource);

		//检查父元素是否存在
		if (Objects.nonNull(permissionResource.getParent()) && permissionResource.getParent() != 0) {
			PermissionResource parentResource = queryByIdFromCache(permissionResource.getParent());
			if (Objects.isNull(parentResource)) {
				log.error("PERMISSION ERROR! permission no parent!,uid={},parentId={}", uid, permissionResource.getParent());
				return Pair.of(false, "父元素不存在");
			}
		}

		if (Objects.nonNull(permissionResource.getMethod()) && Objects.equals(permissionResourceQuery.getType(), PermissionResource.TYPE_URL) && isIllegalMethod(permissionResource.getMethod())) {
			return Pair.of(false, "方法不合法！");
		}

		permissionResource.setUpdateTime(System.currentTimeMillis());
		Integer update = update(permissionResource);
		return update > 0 ? Pair.of(true, null) : Pair.of(false, "更新失败!");
	}

	@Override
	public Pair<Boolean, Object> deletePermission(Long permissionId) {
		PermissionResource permissionResource = queryByIdFromCache(permissionId);

		if (Objects.isNull(permissionResource)) {
			return Pair.of(false, "未能查到相关权限");
		}
		//这里可以直接删除，不用关乎角色。
		if (deleteById(permissionId)) {
			return Pair.of(true, null);
		}

		return Pair.of(false, "删除失败!");
	}

	@Override
	public Pair<Boolean, Object> bindPermissionToRole(Long roleId, List<Long> pids) {
		Role role = roleService.queryByIdFromDB(roleId);
		if (Objects.isNull(role)) {
			return Pair.of(false, "角色查询不到！");
		}
		//删除旧的
		rolePermissionService.deleteByRoleId(roleId);

		List<PermissionResource> permissionResources = queryListByIds(pids);
		if (!DataUtil.collectionIsUsable(permissionResources)) {
			return Pair.of(false, "权限查询不到！");
		}

		HashSet<Long> result = Sets.newHashSet();

		permissionResources.parallelStream().forEach(e -> {
			RolePermission rolePermission = RolePermission.builder()
					.pId(e.getId())
					.roleId(roleId)
					.build();
			rolePermissionService.insert(rolePermission);
			result.add(e.getId());
		});

		//找缓存,过滤重复元素
		redisService.set(ElectricityCabinetConstant.CACHE_ROLE_PERMISSION_RELATION + role.getId(), JsonUtil.toJson(result));

		return Pair.of(true, null);
	}

	private List<PermissionResource> queryListByIds(List<Long> pids) {
		return this.permissionResourceMapper.queryListByIds(pids);
	}

	@Override
	@DS("slave_1")
	public Pair<Boolean, Object> getList() {
		TokenUser userInfo = SecurityUtils.getUserInfo();

		List<PermissionResource> permissionResources = this.permissionResourceMapper.queryAll();
		if (!DataUtil.collectionIsUsable(permissionResources)) {
			return Pair.of(false, "查询不到任何权限！");
		}

		//如果不是超级管理员，就不用返回前4个权限
		if (!Objects.equals(userInfo.getType(), User.TYPE_USER_SUPER)) {
			permissionResources = permissionResources.stream().filter(e -> e.getId() > 4).collect(Collectors.toList());
		}

		List<PermissionResourceTree> permissionResourceTrees = TreeUtils.buildTree(permissionResources, PermissionResource.MENU_ROOT);
		//排序

		return Pair.of(true, permissionResourceTrees);
	}

	@Override
	public Pair<Boolean, Object> getPermissionsByRole(Long rid) {

		List<PermissionResource> permissionResources = queryPermissionsByRole(rid);
		if (DataUtil.collectionIsUsable(permissionResources)) {
			return Pair.of(true, permissionResources.stream().map(PermissionResource::getId).collect(Collectors.toList()));
		}
		return Pair.of(true, Collections.emptyList());
	}

	@Override
	public List<PermissionResource> queryPermissionsByRole(Long rid) {
		List<Long> permissionIds = rolePermissionService.queryPidsByRid(rid);
		ArrayList<PermissionResource> result = Lists.newArrayList();

		if (DataUtil.collectionIsUsable(permissionIds)) {
			permissionIds.forEach(e -> {
				PermissionResource permissionResource = queryByIdFromCache(e);
				if (Objects.nonNull(permissionResource)) {
					result.add(permissionResource);
				}
			});
		}
		return result;
	}

}
