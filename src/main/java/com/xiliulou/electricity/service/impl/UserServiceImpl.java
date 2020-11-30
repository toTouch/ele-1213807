package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.UserMapper;
import com.xiliulou.electricity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * (User)表服务实现类
 *
 * @author makejava
 * @since 2020-11-27 11:19:51
 */
@Service("userService")
@Slf4j
public class UserServiceImpl implements UserService {
	@Resource
	private UserMapper userMapper;
	@Autowired
	RedisService redisService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param uid 主键
	 * @return 实例对象
	 */
	@Override
	public User queryByIdFromDB(Long uid) {
		return this.userMapper.queryById(uid);
	}

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param uid 主键
	 * @return 实例对象
	 */
	@Override
	public User queryByIdFromCache(Long uid) {
		User cacheUser = redisService.getWithHash(ElectricityCabinetConstant.CACHE_USER_UID + uid, User.class);
		if (Objects.nonNull(cacheUser)) {
			return cacheUser;
		}

		User user = queryByIdFromDB(uid);
		if (Objects.isNull(user)) {
			return null;
		}

		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_UID + uid, user);
		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone(), user);

		return user;
	}

	/**
	 * 查询多条数据
	 *
	 * @param offset 查询起始位置
	 * @param limit  查询条数
	 * @return 对象列表
	 */
	@Override
	public List<User> queryAllByLimit(int offset, int limit) {
		return this.userMapper.queryAllByLimit(offset, limit);
	}

	/**
	 * 新增数据
	 *
	 * @param user 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public User insert(User user) {
		this.userMapper.insert(user);
		return user;
	}

	/**
	 * 修改数据
	 *
	 * @param user 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(User user) {
		return this.userMapper.update(user);

	}

	/**
	 * 通过主键删除数据
	 *
	 * @param uid 主键
	 * @return 是否成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean deleteById(Long uid) {
		return this.userMapper.deleteById(uid) > 0;
	}

	@Override
	public User queryByUserName(String username) {
		return this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, username));
	}
}