package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.utils.AESUtils;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.UserMapper;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.security.component.CustomPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
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

	@Autowired
	CustomPasswordEncoder customPasswordEncoder;
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
	@DS("master")
	public User insert(User user) {
		int insert = this.userMapper.insert(user);
		DbUtils.dbOperateSuccessThen(insert, () -> {
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_UID + user.getUid(), user);
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone(), user);
			return user;
		});

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

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Triple<Boolean, String, Object> addAdminUser(AdminUserQuery adminUserQuery) {
		User phoneUserExists = queryByUserPhone(adminUserQuery.getPhone());
		if (Objects.nonNull(phoneUserExists)) {
			return Triple.of(false, null, "用户名已存在");
		}

		User userNameExists = queryByUserName(adminUserQuery.getName());
		if (Objects.nonNull(userNameExists)) {
			return Triple.of(false, null, "手机号已存在");
		}
		//解密密码
		String encryptPassword = adminUserQuery.getPassword();
		String decryptPassword = AESUtils.decrypt(encryptPassword);
		if (StrUtil.isEmpty(decryptPassword)) {
			log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", adminUserQuery.getName(), adminUserQuery.getPhone(), adminUserQuery.getPassword());
			return Triple.of(false, "SYSTEM.0001", "系统错误!");
		}

		User user = User.builder()
				.avatar("")
				.createTime(System.currentTimeMillis())
				.delFlag(User.DEL_NORMAL)
				.lockFlag(User.USER_UN_LOCK)
				.gender(adminUserQuery.getGender())
				.lang(adminUserQuery.getLang())
				.loginPwd(customPasswordEncoder.encode(decryptPassword))
				.name(adminUserQuery.getName())
				.phone(adminUserQuery.getPhone())
				.updateTime(System.currentTimeMillis())
				.userType(adminUserQuery.getUserType())
				.salt("")
				.build();
		//设置角色
		User insert = insert(user);

		return insert.getUid() != null ? Triple.of(true, null, null) : Triple.of(false, null, "保存失败!");
	}

	@Override
	public User queryByUserPhone(String phone) {
		User cacheUser = redisService.getWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + phone, User.class);
		if (Objects.nonNull(cacheUser)) {
			return cacheUser;
		}

		User user = this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
		if (Objects.isNull(user)) {
			return null;
		}

		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_UID + user.getUid(), user);
		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone(), user);

		return user;
	}

	@Override
	@DS("slave_1")
	public Pair<Boolean, Object> queryListUser(Long uid, Integer size, Integer offset, String name, String phone, Integer type) {
		List<User> user = this.userMapper.queryListUserByCriteria(uid, size, offset, name, phone, type);
		return Pair.of(true, DataUtil.collectionIsUsable(user) ? user : Collections.emptyList());
	}

	@Override
	public Pair<Boolean, Object> updateAdminUser(AdminUserQuery adminUserQuery) {
		User user = queryByIdFromCache(adminUserQuery.getUid());
		if (Objects.isNull(user)) {
			return Pair.of(false, "uid:" + adminUserQuery.getUid() + "用户不存在!");
		}

		if (StrUtil.isNotEmpty(adminUserQuery.getPhone())) {
			User phone = queryByUserPhone(adminUserQuery.getPhone());
			if (!Objects.equals(phone.getUid(), adminUserQuery.getUid())) {
				return Pair.of(false,"手机号已存在！无法修改!");
			}
		}
		return null;
	}
}