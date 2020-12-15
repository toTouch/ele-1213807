package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.PermissionResource;
import com.xiliulou.electricity.entity.RolePermission;
import com.xiliulou.electricity.mapper.RolePermissionMapper;
import com.xiliulou.electricity.service.PermissionResourceService;
import com.xiliulou.electricity.service.RolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.print.attribute.standard.PresentationDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (RolePermission)表服务实现类
 *
 * @author makejava
 * @since 2020-12-09 14:36:22
 */
@Service("rolePermissionService")
@Slf4j
public class RolePermissionServiceImpl implements RolePermissionService {
	@Resource
	private RolePermissionMapper rolePermissionMapper;
	@Autowired
	RedisService redisService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public RolePermission queryByIdFromDB(Long id) {
		return this.rolePermissionMapper.queryById(id);
	}

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public RolePermission queryByIdFromCache(Long id) {
		return null;
	}

	/**
	 * 查询多条数据
	 *
	 * @param offset 查询起始位置
	 * @param limit  查询条数
	 * @return 对象列表
	 */
	@Override
	public List<RolePermission> queryAllByLimit(int offset, int limit) {
		return this.rolePermissionMapper.queryAllByLimit(offset, limit);
	}

	/**
	 * 新增数据
	 *
	 * @param rolePermission 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public RolePermission insert(RolePermission rolePermission) {
		this.rolePermissionMapper.insertOne(rolePermission);
		return rolePermission;
	}

	/**
	 * 修改数据
	 *
	 * @param rolePermission 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(RolePermission rolePermission) {
		return this.rolePermissionMapper.update(rolePermission);

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
		return this.rolePermissionMapper.deleteById(id) > 0;
	}

	@Override
	public List<Long> queryPidsByRid(Long rid) {
		String pids = redisService.get(ElectricityCabinetConstant.CACHE_ROLE_PERMISSION_RELATION + rid);
		if (StrUtil.isNotEmpty(pids)) {
			return JsonUtil.fromJsonArray(pids, Long.class);
		}

		List<RolePermission> rolePermissions = this.rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, rid));
		if (!DataUtil.collectionIsUsable(rolePermissions)) {
			return null;
		}

		List<Long> pidsResult = rolePermissions.stream().map(RolePermission::getPId).collect(Collectors.toList());
		redisService.set(ElectricityCabinetConstant.CACHE_ROLE_PERMISSION_RELATION + rid, JsonUtil.toJson(pidsResult));
		return pidsResult;
	}

	@Override
	public boolean deleteByRoleIdAndPermissionId(Long rolePerId, Long permissionId) {
		return rolePermissionMapper.deleteByRoleIdAndPermissionId(rolePerId, permissionId) > 0;
	}

	@Override
	public boolean deleteByRoleId(Long roleId) {
		return rolePermissionMapper.deleteByRoleId(roleId) > 0;
	}
}