package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.api.client.util.Sets;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.PermissionResourceTree;
import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserRole;
import com.xiliulou.electricity.mapper.RoleMapper;
import com.xiliulou.electricity.service.PermissionResourceService;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.TreeUtils;
import com.xiliulou.electricity.web.query.RoleQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (Role)表服务实现类
 *
 * @author makejava
 * @since 2020-12-09 14:34:00
 */
@Service("roleService")
@Slf4j
public class RoleServiceImpl implements RoleService {
	@Resource
	private RoleMapper roleMapper;
	@Autowired
	UserRoleService userRoleService;
	@Autowired
	RoleService roleService;
	@Autowired
	UserService userService;
	@Autowired
	RedisService redisService;
	@Autowired
	PermissionResourceService permissionResourceService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public Role queryByIdFromDB(Long id) {
		return this.roleMapper.queryById(id);
	}



	/**
	 * 新增数据
	 *
	 * @param role 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Role insert(Role role) {
		this.roleMapper.insertOne(role);
		return role;
	}

	/**
	 * 修改数据
	 *
	 * @param role 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(Role role) {
		return this.roleMapper.update(role);

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
		return this.roleMapper.deleteById(id) > 0;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteById(Long id,Integer tenantId) {
		return this.roleMapper.deleteById(id,tenantId) > 0;
	}
//
	@Override
	public R addRole(RoleQuery roleQuery) {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		Role role = new Role();
		BeanUtils.copyProperties(roleQuery, role);

		role.setUpdateTime(System.currentTimeMillis());
		role.setCreateTime(System.currentTimeMillis());
		role.setTenantId(tenantId);

		int insert = roleMapper.insertOne(role);
		return insert > 0 ? R.ok() : R.fail("保存失败！");
	}
//
	@Override
	public R updateRole(RoleQuery roleQuery) {
		Role role = new Role();
		BeanUtils.copyProperties(roleQuery, role);
		role.setTenantId(TenantContextHolder.getTenantId());
		role.setUpdateTime(System.currentTimeMillis());

		Integer update = update(role);
		return update > 0 ? R.ok() : R.fail("更新失败！");
	}
//
	@Override
	public Pair<Boolean, Object> deleteRole(Long id) {
		Role role = queryByIdFromDB(id);
		if (Objects.isNull(role)) {
			return Pair.of(false, "该id的角色不存在!");
		}

		if (userRoleService.existsRole(id)) {
			return Pair.of(false, "无法删除！请先解绑绑定该角色的用户");
		}
		
		if (deleteById(id, TenantContextHolder.getTenantId())) {
			return Pair.of(true, null);
		}

		return Pair.of(false, "删除失败!");
	}
//
	@Override
	public Pair<Boolean, Object> bindUserRole(Long uid, List<Long> roleIds) {
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			return Pair.of(false, "用户不存在，无法绑定！");
		}
		userRoleService.deleteByUid(uid);

		List<Role> roles = this.roleMapper.queryByRoleIds(roleIds);
		if (!DataUtil.collectionIsUsable(roles)) {
			return Pair.of(false, "角色不存在，无法绑定！");
		}

		roles.parallelStream().forEach(r -> {
			UserRole userRole = UserRole.builder()
					.uid(uid)
					.roleId(r.getId())
					.build();
			userRoleService.insert(userRole);
		});

		HashSet<Long> result = Sets.newHashSet();
		result.addAll(roleIds);
		redisService.set(CacheConstant.CACHE_USER_ROLE_RELATION + uid, JsonUtil.toJson(result));

		return Pair.of(true, "绑定成功!");
	}


	@Override
	public Pair<Boolean, Object> getMenuByUid() {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return Pair.of(false, "未能查到相关用户！");
		}

		List<Long> rids = queryRidsByUid(uid);
		if (!DataUtil.collectionIsUsable(rids)) {
			return Pair.of(true, Collections.emptyList());
		}

		ArrayList<PermissionResource> result = Lists.newArrayList();

		for (Long rid : rids) {
			List<PermissionResource> permissionResources = permissionResourceService.queryPermissionsByRole(rid);
			if (!DataUtil.collectionIsUsable(permissionResources)) {
				continue;
			}
			result.addAll(permissionResources.stream().filter(e -> e.getType().equals(PermissionResource.TYPE_PAGE)).sorted(Comparator.comparing(PermissionResource::getSort)).collect(Collectors.toList()));
		}

		List<PermissionResourceTree> permissionResourceTrees = TreeUtils.buildTree(result, PermissionResource.MENU_ROOT);
		return Pair.of(true, permissionResourceTrees);
	}
//
	@Override
	public Pair<Boolean, Object> queryBindUidRids(Long uid) {
		List<Long> ids = queryRidsByUid(uid);
		return Pair.of(true, ids);
	}
//
	@Override
	public List<Long> queryRidsByUid(Long uid) {
		String jsonRoles = redisService.get(CacheConstant.CACHE_USER_ROLE_RELATION + uid);
		if (!StrUtil.isEmpty(jsonRoles)) {
			return JsonUtil.fromJsonArray(jsonRoles, Long.class);
		}

		List<Role> roles = userRoleService.queryByUid(uid);
		if (DataUtil.collectionIsUsable(roles)) {
			roles = roles.stream()
					.filter(item -> Objects.equals(item.getTenantId(), TenantContextHolder.getTenantId()))
					.collect(Collectors.toList());
			if (CollectionUtils.isEmpty(roles)) {
				return Collections.emptyList();
			}
			
			List<Long> rolesIds = roles.stream().map(Role::getId).collect(Collectors.toList());
			redisService.set(CacheConstant.CACHE_USER_ROLE_RELATION + uid, JsonUtil.toJson(rolesIds));
			return rolesIds;
		}

		return Collections.emptyList();
	}
//
	@Override
	public R queryAll() {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();


		List<Role> roles = this.roleMapper.queryAll(tenantId);
		if (!DataUtil.collectionIsUsable(roles)) {
			return R.ok(Collections.EMPTY_LIST);
		}
		//不显示超级管理员角色
		return R.ok(roles.stream().filter(e -> e.getId() > 1).collect(Collectors.toList()));
	}

	@Override
	public Long queryByName(String name, Integer tenantId) {
		Role role=roleMapper.queryByName(name,tenantId);
		if(Objects.isNull(role)) {
			return null;
		}
		return role.getId();
	}

}
