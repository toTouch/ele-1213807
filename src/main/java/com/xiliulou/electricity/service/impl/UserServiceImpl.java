package com.xiliulou.electricity.service.impl;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.City;
import com.xiliulou.electricity.entity.ElectricityCabinetBind;
import com.xiliulou.electricity.entity.Province;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserRole;
import com.xiliulou.electricity.mapper.UserMapper;
import com.xiliulou.electricity.query.BindElectricityCabinetQuery;
import com.xiliulou.electricity.service.CityService;
import com.xiliulou.electricity.service.FranchiseeBindElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBindService;
import com.xiliulou.electricity.service.ProvinceService;
import com.xiliulou.electricity.service.StoreBindService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserVo;
import com.xiliulou.electricity.web.query.AdminUserQuery;
import com.xiliulou.electricity.web.query.PasswordQuery;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	UserRoleService userRoleService;

	@Autowired
	CityService cityService;

	@Autowired
	ProvinceService provinceService;

	@Autowired
	ElectricityCabinetBindService electricityCabinetBindService;

	@Autowired
	FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;

	@Autowired
	StoreBindService storeBindService;

	@Value("${security.encode.key:xiliu&lo@u%12345}")
	private String encodeKey;

	@Autowired
	UserOauthBindService userOauthBindService;

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
	public User queryByUidFromCache(Long uid) {
		User cacheUser = redisService.getWithHash(ElectricityCabinetConstant.CACHE_USER_UID + uid, User.class);
		if (Objects.nonNull(cacheUser)) {
			return cacheUser;
		}

		User user = queryByIdFromDB(uid);
		if (Objects.isNull(user)) {
			return null;
		}

		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_UID + uid, user);
		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType(), user);

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
		int insert = this.userMapper.insertOne(user);
		DbUtils.dbOperateSuccessThen(insert, () -> {
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_UID + user.getUid(), user);
			redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType(), user);
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
	@Transactional(rollbackFor = Exception.class)
	public Integer update(User user) {
		return this.userMapper.updateById(user);

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
		return this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getName, username).eq(User::getDelFlag, User.DEL_NORMAL));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Triple<Boolean, String, Object> addAdminUser(AdminUserQuery adminUserQuery) {
		TokenUser tokenUser = SecurityUtils.getUserInfo();
		if (Objects.isNull(tokenUser)) {
			return Triple.of(false, "USER.0001", "查询不到用户！");
		}

		if (!Objects.equals(tokenUser.getType(), User.TYPE_USER_SUPER) && Objects.equals(adminUserQuery.getUserType(), User.TYPE_USER_SUPER)) {
			return Triple.of(false, null, "无权限操作！");
		}

		User phoneUserExists = queryByUserPhone(adminUserQuery.getPhone(), adminUserQuery.getUserType());
		if (Objects.nonNull(phoneUserExists)) {
			return Triple.of(false, null, "手机号已存在");
		}

		User userNameExists = queryByUserName(adminUserQuery.getName());
		if (Objects.nonNull(userNameExists)) {
			return Triple.of(false, null, "用户名已存在");
		}

		//解密密码
		String encryptPassword = adminUserQuery.getPassword();
		String decryptPassword = decryptPassword(encryptPassword);
		if (StrUtil.isEmpty(decryptPassword)) {
			log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", adminUserQuery.getName(), adminUserQuery.getPhone(), adminUserQuery.getPassword());
			return Triple.of(false, "SYSTEM.0001", "系统错误!");
		}

		//处理城市和省份
		City city = cityService.queryByIdFromDB(adminUserQuery.getCityId());
		Province province = provinceService.queryByIdFromDB(adminUserQuery.getProvinceId());

		User user = User.builder()
				.avatar("")
				.salt("")
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
				.city(Objects.nonNull(city) ? city.getName() : null)
				.province(Objects.nonNull(province) ? province.getName() : null)
				.cid(Objects.nonNull(city) ? city.getId() : null)
				.build();
		User insert = insert(user);

		//设置角色
		UserRole userRole = new UserRole();
		userRole.setRoleId(adminUserQuery.getUserType().longValue() + 1);
		userRole.setUid(insert.getUid());
		userRoleService.insert(userRole);


		return insert.getUid() != null ? Triple.of(true, null, null) : Triple.of(false, null, "保存失败!");
	}

	private String decryptPassword(String encryptPassword) {
		AES aes = new AES(Mode.CBC, Padding.ZeroPadding, new SecretKeySpec(encodeKey.getBytes(), "AES"),
				new IvParameterSpec(encodeKey.getBytes()));

		return new String(aes.decrypt(Base64.decode(encryptPassword.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
	}

	@Override
	public User queryByUserPhone(String phone, Integer type) {
		User cacheUser = redisService.getWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + phone + ":" + type, User.class);
		if (Objects.nonNull(cacheUser)) {
			return cacheUser;
		}

		User user = this.userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone).eq(User::getUserType, type).eq(User::getDelFlag, User.DEL_NORMAL));
		if (Objects.isNull(user)) {
			return null;
		}

		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_UID + user.getUid(), user);
		redisService.saveWithHash(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType(), user);

		return user;
	}

	@Override
	@DS("slave_1")
	public Pair<Boolean, Object> queryListUser(Long uid, Long size, Long offset, String name, String phone, Integer type, Long startTime, Long endTime) {
		Page page = PageUtil.getPage(offset, size);

		return Pair.of(true, this.userMapper.queryListUserByCriteria(page, uid, size, offset, name, phone, type, startTime, endTime));

	}

	@Override
	public Pair<Boolean, Object> updateAdminUser(AdminUserQuery adminUserQuery) {
		User user = queryByUidFromCache(adminUserQuery.getUid());
		if (Objects.isNull(user)) {
			return Pair.of(false, "uid:" + adminUserQuery.getUid() + "用户不存在!");
		}

		if (StrUtil.isNotEmpty(adminUserQuery.getPhone())) {
			User phone = queryByUserPhone(adminUserQuery.getPhone(), User.TYPE_USER_OPERATE);
			if (Objects.nonNull(phone) && !Objects.equals(phone.getUid(), adminUserQuery.getUid())) {
				return Pair.of(false, "手机号已存在！无法修改!");
			}
		}

		if (StrUtil.isNotEmpty(adminUserQuery.getName())) {
			User nameUser = queryByUserName(adminUserQuery.getName());
			if (Objects.nonNull(nameUser) && !Objects.equals(nameUser.getUid(), adminUserQuery.getUid())) {
				return Pair.of(false, "用户名已经存在！无法修改！");
			}
		}

		String decryptPassword = null;
		if (StrUtil.isNotEmpty(adminUserQuery.getPassword())) {
			//解密密码
			String encryptPassword = adminUserQuery.getPassword();
			decryptPassword = decryptPassword(encryptPassword);
			if (StrUtil.isEmpty(decryptPassword)) {
				log.error("ADMIN USER ERROR! decryptPassword error! username={},phone={},password={}", adminUserQuery.getName(), adminUserQuery.getPhone(), adminUserQuery.getPassword());
				return Pair.of(false, "系统错误!");
			}
		}

		User updateUser = User.builder()
				.uid(user.getUid())
				.delFlag(User.DEL_NORMAL)
				.lockFlag(User.USER_UN_LOCK)
				.gender(adminUserQuery.getGender())
				.lang(adminUserQuery.getLang())
				.loginPwd(StrUtil.isEmpty(decryptPassword) ? null : customPasswordEncoder.encode(decryptPassword))
				.name(adminUserQuery.getName())
				.phone(adminUserQuery.getPhone())
				.updateTime(System.currentTimeMillis())
				.userType(adminUserQuery.getUserType())
				.lockFlag(adminUserQuery.getLock())
				.build();

		if(Objects.nonNull(adminUserQuery.getCityId())) {
			//城市
			City city = cityService.queryByIdFromDB(adminUserQuery.getCityId());
			if (Objects.nonNull(city)) {
				Province province = provinceService.queryByIdFromDB(city.getPid());
				if (Objects.nonNull(province)) {
					updateUser.setCid(adminUserQuery.getCityId());
					updateUser.setCity(city.getName());
					updateUser.setProvince(province.getName());
				}
			}
		}

		int i = updateUser(updateUser, user);
		//更新userInfo
		if (i > 0) {
			UserInfo oldUserInfo = userInfoService.queryByUid(user.getUid());
			if (Objects.nonNull(oldUserInfo)) {
				UserInfo userInfo = new UserInfo();
				userInfo.setId(oldUserInfo.getId());
				userInfo.setUpdateTime(System.currentTimeMillis());
				userInfo.setPhone(updateUser.getPhone());
				userInfoService.update(userInfo);
			}
		}
		return i > 0 ? Pair.of(true, null) : Pair.of(false, "更新失败!");
	}

	@Override
	public Pair<Boolean, Object> deleteAdminUser(Long uid) {
		User user = queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			return Pair.of(false, "uid:" + uid + "用户不存在!");
		}
		if(Objects.equals(user.getUserType(),User.TYPE_USER_NORMAL_WX_PRO)){
			return Pair.of(false, "非法操作");
		}

		if (deleteById(uid)) {
			redisService.delete(ElectricityCabinetConstant.CACHE_USER_UID + uid);
			redisService.delete(ElectricityCabinetConstant.CACHE_USER_PHONE + user.getPhone() + ":" + user.getUserType());
		}
		return Pair.of(true, null);
	}

	@Override
	public Triple<Boolean, String, Object> updatePassword(PasswordQuery passwordQuery) {
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("updatePassword  ERROR! not found user ");
			Triple.of(false, "ELECTRICITY.0001", "未找到用户");
		}

		User oldUser = queryByUserName(passwordQuery.getName());
		if (Objects.isNull(oldUser)) {
			log.error("updatePassword  ERROR! not found userName{} ", passwordQuery.getName());
			Triple.of(false, null, "用户名不存在");
		}

		if (!Objects.equals(user, oldUser)) {
			log.error("updatePassword  ERROR! not found userId{},oldUserId{} ", user.getUid(), oldUser.getUid());
			Triple.of(false, null, "不能修改别人的密码");
		}

		User updateUser = new User();
		updateUser.setUid(oldUser.getUid());
		updateUser.setLoginPwd(customPasswordEncoder.encode(passwordQuery.getPassword()));
		updateUser.setUpdateTime(System.currentTimeMillis());
		Integer update = updateUser(updateUser, oldUser);

		return update > 0 ? Triple.of(true, null, null) : Triple.of(false, null, "修改密码失败!");
	}

	@Override
	public Integer updateUser(User updateUser, User oldUser) {
		Integer update = update(updateUser);
		if (update > 0) {
			redisService.delete(ElectricityCabinetConstant.CACHE_USER_UID + oldUser.getUid());
			redisService.delete(ElectricityCabinetConstant.CACHE_USER_PHONE + oldUser.getPhone() + ":" + oldUser.getUserType());
		}
		return update;
	}

	@Override
	public Pair<Boolean, Object> addUserAddress(String cityCode) {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			log.error("USER ERROR! no user!");
			return Pair.of(false, "未能查到用户信息!");
		}

		User user = queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("USER ERROR! no user! uid={}", uid);
			return Pair.of(false, "未能查到用户信息!");
		}

		City city = cityService.queryCityByCode(cityCode);
		if (Objects.isNull(city)) {
			log.error("USER ERROR! no city! cityCode={},uid={}", cityCode, uid);
			return Pair.of(false, "未能查到相关城市信息");
		}

		Province province = provinceService.queryByIdFromDB(city.getPid());
		if (Objects.isNull(province)) {
			log.error("USER ERROR! no province! provinceId={},uid={}", city.getPid(), uid);
			return Pair.of(false, "未能查到相关省份信息");
		}

		User updateUser = new User();
		updateUser.setUid(user.getUid());
		updateUser.setCid(city.getId());
		updateUser.setCity(city.getName());
		updateUser.setProvince(province.getName());
		updateUser.setUpdateTime(System.currentTimeMillis());
		updateUser(updateUser, user);

		return Pair.of(true, null);
	}

	@Override
	public Pair<Boolean, Object> getUserDetail() {
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			log.error("USER ERROR! no use!");
			return Pair.of(false, "USER.0001");
		}

		User user = queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("USER ERROR! no found user! uid={}", uid);
			return Pair.of(false, "USER.0001");
		}

		HashMap<String, Object> resultMap = Maps.newHashMap();

		//处理用户信息
		UserVo userVo = new UserVo();
		BeanUtils.copyProperties(user, userVo);
		resultMap.put("user", userVo);

		return Pair.of(true, resultMap);
	}

	@Override
	public R bindElectricityCabinet(BindElectricityCabinetQuery bindElectricityCabinetQuery) {
		//先删除
		electricityCabinetBindService.deleteByUid(bindElectricityCabinetQuery.getUid());
		if(ObjectUtil.isEmpty(bindElectricityCabinetQuery.getElectricityCabinetIdList())){
			return R.ok();
		}
		//再新增
		for (Integer electricityCabinetId : bindElectricityCabinetQuery.getElectricityCabinetIdList()) {
			ElectricityCabinetBind electricityCabinetBind=new ElectricityCabinetBind();
			electricityCabinetBind.setUid(bindElectricityCabinetQuery.getUid());
			electricityCabinetBind.setElectricityCabinetId(electricityCabinetId);
			electricityCabinetBindService.insert(electricityCabinetBind);
		}
		return R.ok();
	}

	@Override
	public R queryElectricityCabinetList(Long uid) {
		return R.ok(electricityCabinetBindService.queryElectricityCabinetList(uid));
	}

	@Override
	public Pair<Boolean, Object> listByFranchisee(Long uid, Long size, Long offset, String name, String phone, Integer type, Long startTime, Long endTime, List<Integer> cidList) {
		Page page = PageUtil.getPage(offset, size);

		return Pair.of(true, this.userMapper.listByFranchisee(page, uid, size, offset, name, phone, type, startTime, endTime,cidList));
	}

	@Override
	public R endLimitUser(Long uid) {
		String orderLimit = redisService.get(ElectricityCabinetConstant.ORDER_TIME_UID+uid);
		if (Objects.isNull(orderLimit)) {
			return R.fail("ELECTRICITY.0062", "用户未被限制");
		}
		redisService.delete(ElectricityCabinetConstant.ORDER_TIME_UID +uid);
		return R.ok();
	}

}
