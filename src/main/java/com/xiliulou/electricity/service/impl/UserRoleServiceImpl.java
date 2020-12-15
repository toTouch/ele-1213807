package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.Role;
import com.xiliulou.electricity.entity.UserRole;
import com.xiliulou.electricity.mapper.UserRoleMapper;
import com.xiliulou.electricity.service.RoleService;
import com.xiliulou.electricity.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * (UserRole)表服务实现类
 *
 * @author makejava
 * @since 2020-12-09 14:19:42
 */
@Service("userRoleService")
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {
	@Resource
	private UserRoleMapper userRoleMapper;
	@Autowired
	private RoleService roleService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public UserRole queryByIdFromDB(Long id) {
		return this.userRoleMapper.queryById(id);
	}

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public UserRole queryByIdFromCache(Long id) {
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
	public List<UserRole> queryAllByLimit(int offset, int limit) {
		return this.userRoleMapper.queryAllByLimit(offset, limit);
	}

	/**
	 * 新增数据
	 *
	 * @param userRole 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public UserRole insert(UserRole userRole) {
		this.userRoleMapper.insertOne(userRole);
		return userRole;
	}

	/**
	 * 修改数据
	 *
	 * @param userRole 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(UserRole userRole) {
		return this.userRoleMapper.update(userRole);

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
		return this.userRoleMapper.deleteById(id) > 0;
	}

	@Override
	@DS("slave_1")
	public List<Role> queryByUid(Long uid) {
		List<UserRole> userRoles = this.userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUid, uid));
		if (!DataUtil.collectionIsUsable(userRoles)) {
			return null;
		}
		ArrayList<Role> useRoles = Lists.newArrayList();
		userRoles.forEach(e -> {
			Role role = this.roleService.queryByIdFromDB(e.getRoleId());
			if (Objects.isNull(role)) {
				return;
			}
			useRoles.add(role);
		});

		return useRoles;
	}

	@Override
	@DS("slave_1")
	public boolean existsRole(Long id) {
		Integer count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
		return count != null && count > 0;
	}

	@Override
	public boolean deleteByUid(Long uid) {
		return userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUid, uid)) > 0;
	}
}