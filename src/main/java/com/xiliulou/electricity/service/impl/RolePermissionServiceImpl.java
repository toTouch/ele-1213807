package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.RolePermission;
import com.xiliulou.electricity.mapper.RolePermissionMapper;
import com.xiliulou.electricity.service.RolePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

import java.util.List;
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
	 * 新增数据
	 *
	 * @param rolePermission 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public RolePermission insert(RolePermission rolePermission) {
		this.rolePermissionMapper.insert(rolePermission);
		return rolePermission;
	}



	@Override
	public List<Long> queryPidsByRid(Long rid) {
		String pids = redisService.get(CacheConstant.CACHE_ROLE_PERMISSION_RELATION + rid);
		if (StrUtil.isNotEmpty(pids)) {
			return JsonUtil.fromJsonArray(pids, Long.class);
		}

		List<RolePermission> rolePermissions = this.rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, rid));
		if (!DataUtil.collectionIsUsable(rolePermissions)) {
			return null;
		}

		List<Long> pidsResult = rolePermissions.stream().map(RolePermission::getPId).collect(Collectors.toList());
		redisService.set(CacheConstant.CACHE_ROLE_PERMISSION_RELATION + rid, JsonUtil.toJson(pidsResult));
		return pidsResult;
	}

	@Override
	public boolean deleteByRoleId(Long roleId) {
		return rolePermissionMapper.deleteByRoleId(roleId) > 0;
	}


}
