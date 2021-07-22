package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.OwnMemberCardInfoVo;
import com.xiliulou.electricity.vo.UserAuthInfoVo;
import com.xiliulou.electricity.vo.UserInfoVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户列表(TUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("userInfoService")
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
	@Resource
	private UserInfoMapper userInfoMapper;
	@Autowired
	StoreService storeService;
	@Autowired
	RentBatteryOrderService rentBatteryOrderService;
	@Autowired
	ElectricityBatteryService electricityBatteryService;
	@Autowired
	RedisService redisService;
	@Autowired
	ElectricityMemberCardOrderService electricityMemberCardOrderService;
	@Autowired
	UserService userService;
	@Autowired
	EleUserAuthService eleUserAuthService;
	@Autowired
	FranchiseeUserInfoService franchiseeUserInfoService;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public UserInfo queryByIdFromDB(Long id) {
		return this.userInfoMapper.selectById(id);
	}

	/**
	 * @param id 主键
	 * @return
	 */
	@Override
	public UserInfo selectUserByUid(Long id) {
		UserInfo userInfo =
				this.userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUid, id));

		return userInfo;
	}

	/**
	 * 新增数据
	 *
	 * @param userInfo 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer insert(UserInfo userInfo) {
		return userInfoMapper.insert(userInfo);
	}

	/**
	 * 修改数据
	 *
	 * @param userInfo 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(UserInfo userInfo) {
		return this.userInfoMapper.updateById(userInfo);

	}


	@Override
	@DS("slave_1")
	public R queryList(UserInfoQuery userInfoQuery) {
		List<UserInfo> userInfoList = userInfoMapper.queryList(userInfoQuery);
		if (ObjectUtil.isEmpty(userInfoList)) {
			return R.ok(userInfoList);
		}

		List<UserInfoVO> userInfoVOList=new ArrayList<>();
		for (UserInfo userInfo:userInfoList) {
			UserInfoVO userInfoVO=new UserInfoVO();
			FranchiseeUserInfo franchiseeUserInfo=franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());
			if(Objects.nonNull(franchiseeUserInfo)){
				BeanUtil.copyProperties(franchiseeUserInfo,userInfoVO);
				if(!Objects.equals(franchiseeUserInfo.getServiceStatus(),FranchiseeUserInfo.STATUS_IS_INIT)){
					userInfo.setServiceStatus(franchiseeUserInfo.getServiceStatus());
				}
			}
			BeanUtil.copyProperties(userInfo,userInfoVO);

			userInfoVOList.add(userInfoVO);
		}

		userInfoVOList.stream().sorted(Comparator.comparing(UserInfoVO::getCreateTime).reversed()).collect(Collectors.toList());
		return R.ok(userInfoVOList);
	}

	@Override
	@Transactional
	public R updateStatus(Long id,Integer usableStatus) {
		UserInfo oldUserInfo = queryByIdFromDB(id);
		if (Objects.isNull(oldUserInfo)) {
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setId(id);
		userInfo.setUpdateTime(System.currentTimeMillis());
		userInfo.setUsableStatus(usableStatus);
		userInfoMapper.updateById(userInfo);
		return R.ok();
	}



	@Override
	public UserInfo queryByUid(Long uid) {
		return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
	}

	@Override
	public Integer homeOne(Long first, Long now,Integer tenantId) {
		return userInfoMapper.homeOne(first,now,tenantId);
	}



	@Override
	public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay,Integer tenantId) {
		return userInfoMapper.homeThree(startTimeMilliDay, endTimeMilliDay,tenantId);
	}


	/**
	 * 获取用户套餐信息
	 *
	 * @param uid
	 * @return
	 */
	@Override
	@DS("slave_1")
	public R getMemberCardInfo(Long uid) {
		UserInfo userInfo = selectUserByUid(uid);
		if (Objects.isNull(userInfo)) {
			log.error("GET_MEMBER_CARD_INFO ERROR,NOT FOUND USERINFO,UID:{}", uid);
			return R.failMsg("未找到用户信息!");
		}

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(franchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", userInfo.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}

		//判断是否缴纳押金
		if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
				|| Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
			log.error("returnBattery  ERROR! not pay deposit! uid:{} ", userInfo.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}


		if (Objects.isNull(franchiseeUserInfo.getRemainingNumber()) || Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime()) || System.currentTimeMillis() >=
				franchiseeUserInfo.getMemberCardExpireTime() || franchiseeUserInfo.getRemainingNumber() == 0) {
			return R.ok();
		}

		OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
		ownMemberCardInfoVo.setMemberCardExpireTime(franchiseeUserInfo.getMemberCardExpireTime());
		ownMemberCardInfoVo.setRemainingNumber(franchiseeUserInfo.getRemainingNumber());
		ownMemberCardInfoVo.setType(franchiseeUserInfo.getCardType());
		ownMemberCardInfoVo.setName(franchiseeUserInfo.getCardName());
		ownMemberCardInfoVo.setDays((long) Math.round((franchiseeUserInfo.getMemberCardExpireTime() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
		return R.ok(ownMemberCardInfoVo);
	}


	@Override
	public R queryUserInfo() {
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		//2.判断用户是否有电池是否有月卡
		UserInfo userInfo = queryByUid(user.getUid());
		if (Objects.isNull(userInfo)) {
			log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}
		//用户是否可用
		if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! user is unusable! userInfo:{} ", userInfo);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//未实名认证
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("ELECTRICITY  ERROR! not auth! userInfo:{} ", userInfo);
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

		//未找到用户
		if (Objects.isNull(franchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}


		//判断是否缴纳押金
		if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
				|| Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
			log.error("returnBattery  ERROR! not pay deposit! uid:{} ", userInfo.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}


		//判断用户是否开通月卡
		if (Objects.isNull(franchiseeUserInfo.getMemberCardExpireTime())
				|| Objects.isNull(franchiseeUserInfo.getRemainingNumber())) {
			log.error("ELECTRICITY  ERROR! not found memberCard ! uid:{} ", userInfo.getUid());
			return R.fail("ELECTRICITY.0022", "未开通月卡");
		}
		Long now = System.currentTimeMillis();
		if (franchiseeUserInfo.getMemberCardExpireTime() < now
				|| franchiseeUserInfo.getRemainingNumber() == 0) {
			log.error("ELECTRICITY  ERROR! memberCard is  Expire ! uid:{} ", userInfo.getUid());
			return R.fail("ELECTRICITY.0023", "月卡已过期");
		}

		//未租电池
		if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_DEPOSIT)) {
			log.error("ELECTRICITY  ERROR! not rent battery! userInfo:{} ", userInfo);
			return R.fail("ELECTRICITY.0033", "用户未绑定电池");
		}

		return R.ok(userInfo);
	}

	@Override
	public R verifyAuth(Long id, Integer authStatus) {
		UserInfo oldUserInfo = queryByIdFromDB(id);
		if (Objects.isNull(oldUserInfo)) {
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setId(id);
		userInfo.setUpdateTime(System.currentTimeMillis());
		userInfo.setAuthStatus(authStatus);
		if (Objects.equals(authStatus, UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
			userInfo.setServiceStatus(UserInfo.STATUS_IS_AUTH);
		}
		userInfoMapper.updateById(userInfo);
		//修改资料项
		eleUserAuthService.updateByUid(oldUserInfo.getUid(), authStatus);
		return R.ok();
	}


	@Override
	@Transactional
	public R updateAuth(UserInfo userInfo) {
		userInfo.setUpdateTime(System.currentTimeMillis());
		Integer update = update(userInfo);

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		DbUtils.dbOperateSuccessThen(update, () -> {
			//实名认证数据修改
			UserInfo newUserInfo = this.queryByIdFromDB(userInfo.getId());
			//身份证
			if (Objects.nonNull(userInfo.getIdNumber())) {
				EleUserAuth eleUserAuth1 = eleUserAuthService.queryByUidAndEntryId(newUserInfo.getUid(), EleAuthEntry.ID_ID_CARD);
				if (Objects.nonNull(eleUserAuth1)) {
					eleUserAuth1.setUpdateTime(System.currentTimeMillis());
					eleUserAuth1.setValue(userInfo.getIdNumber());
					eleUserAuthService.update(eleUserAuth1);
				} else {
					eleUserAuth1 = new EleUserAuth();
					eleUserAuth1.setUid(userInfo.getUid());
					eleUserAuth1.setEntryId(EleAuthEntry.ID_ID_CARD);
					eleUserAuth1.setUpdateTime(System.currentTimeMillis());
					eleUserAuth1.setValue(userInfo.getIdNumber());
					eleUserAuth1.setCreateTime(System.currentTimeMillis());
					eleUserAuth1.setTenantId(tenantId);
					eleUserAuthService.insert(eleUserAuth1);
				}
			}

			//姓名
			if (Objects.nonNull(userInfo.getName())) {
				EleUserAuth eleUserAuth2 = eleUserAuthService.queryByUidAndEntryId(newUserInfo.getUid(), EleAuthEntry.ID_NAME_ID);
				if (Objects.nonNull(eleUserAuth2)) {
					eleUserAuth2.setUpdateTime(System.currentTimeMillis());
					eleUserAuth2.setValue(userInfo.getName());
					eleUserAuthService.update(eleUserAuth2);
				} else {
					eleUserAuth2 = new EleUserAuth();
					eleUserAuth2.setUid(userInfo.getUid());
					eleUserAuth2.setEntryId(EleAuthEntry.ID_NAME_ID);
					eleUserAuth2.setUpdateTime(System.currentTimeMillis());
					eleUserAuth2.setValue(userInfo.getName());
					eleUserAuth2.setCreateTime(System.currentTimeMillis());
					eleUserAuth2.setTenantId(tenantId);
					eleUserAuthService.insert(eleUserAuth2);
				}
			}
			return null;
		});
		return R.ok();
	}


	@Override
	public R queryUserAuthInfo(UserInfoQuery userInfoQuery) {
		List<UserInfo> userInfos =userInfoMapper.queryList(userInfoQuery);
		if (!DataUtil.collectionIsUsable(userInfos)) {
			return R.ok(Collections.emptyList());
		}

		List<UserAuthInfoVo> result = userInfos.parallelStream().map(e -> {
			UserAuthInfoVo userAuthInfoVo = new UserAuthInfoVo();
			BeanUtils.copyProperties(e, userAuthInfoVo);

			List<EleUserAuth> list = (List<EleUserAuth>) eleUserAuthService.selectCurrentEleAuthEntriesList(e.getUid()).getData();
			if (!DataUtil.collectionIsUsable(list)) {
				return userAuthInfoVo;
			}

			list.stream().forEach(auth -> {
				if (auth.getEntryId().equals(EleAuthEntry.ID_CARD_BACK_PHOTO)) {
					userAuthInfoVo.setIdCardBackUrl(auth.getValue());
				}

				if (auth.getEntryId().equals(EleAuthEntry.ID_CARD_FRONT_PHOTO)) {
					userAuthInfoVo.setIdCardFrontUrl(auth.getValue());
				}

			});

			return userAuthInfoVo;
		}).collect(Collectors.toList());

		return R.ok(result);
	}

	@Override
	public R queryCount(UserInfoQuery userInfoQuery) {
		return R.ok(userInfoMapper.queryCount(userInfoQuery));
	}

	//后台绑定电池
	@Override
	@Transactional
	public R webBindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {

		//查找用户
		UserInfo oldUserInfo = queryByIdFromDB(userInfoBatteryAddAndUpdate.getId());
		if (Objects.isNull(oldUserInfo)) {
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}

		//未实名认证
		if (Objects.equals(oldUserInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("webBindBattery  ERROR! user not auth! uid:{} ", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}


		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(oldUserInfo.getId());

		//未找到用户
		if (Objects.isNull(oldFranchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}

		//判断是否缴纳押金
		if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
				|| Objects.isNull(oldFranchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(oldFranchiseeUserInfo.getOrderId())) {
			log.error("ELECTRICITY  ERROR! not pay deposit! uid:{} ", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}


		//已绑定电池
		if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY)) {
			log.error("webBindBattery  ERROR! user rent battery! uid:{} ", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0045", "已绑定电池");
		}

		//判断电池是否存在，或者已经被绑定
		ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByBindSn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
		if (Objects.isNull(oldElectricityBattery)) {
			log.error("webBindBattery  ERROR! not found Battery! sn:{} ", userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
			return R.fail("ELECTRICITY.0020", "未找到电池");
		}


		//绑定电池
		FranchiseeUserInfo franchiseeUserInfo = new FranchiseeUserInfo();
		franchiseeUserInfo.setId(oldFranchiseeUserInfo.getId());
		if (Objects.isNull(oldFranchiseeUserInfo.getInitElectricityBatterySn())) {
			franchiseeUserInfo.setInitElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
		}
		franchiseeUserInfo.setNowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
		franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
		franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_BATTERY);
		Integer update = franchiseeUserInfoService.update(franchiseeUserInfo);

		DbUtils.dbOperateSuccessThen(update, () -> {
			RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();

			//添加租电池记录
			rentBatteryOrder.setUid(oldUserInfo.getUid());
			rentBatteryOrder.setName(oldUserInfo.getName());
			rentBatteryOrder.setPhone(oldUserInfo.getPhone());
			rentBatteryOrder.setElectricityBatterySn(franchiseeUserInfo.getInitElectricityBatterySn());
			rentBatteryOrder.setBatteryDeposit(oldFranchiseeUserInfo.getBatteryDeposit());
			rentBatteryOrder.setCreateTime(System.currentTimeMillis());
			rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
			rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_BIND);
			rentBatteryOrderService.insert(rentBatteryOrder);

			//修改电池状态
			ElectricityBattery electricityBattery = new ElectricityBattery();
			electricityBattery.setId(oldElectricityBattery.getId());
			electricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
			electricityBattery.setElectricityCabinetId(null);
			electricityBattery.setUid(rentBatteryOrder.getUid());
			electricityBattery.setUpdateTime(System.currentTimeMillis());
			electricityBatteryService.updateByOrder(electricityBattery);
			return null;
		});
		return R.ok();
	}

	@Override
	@Transactional
	public R webUnBindBattery(Long id) {
		//查找用户
		UserInfo oldUserInfo = queryByIdFromDB(id);
		if (Objects.isNull(oldUserInfo)) {
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}

		//未实名认证
		if (Objects.equals(oldUserInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("webUnBindBattery  ERROR! user not auth! uid:{} ", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}


		//是否缴纳押金，是否绑定电池
		FranchiseeUserInfo oldFranchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(oldUserInfo.getId());

		//未找到用户
		if (Objects.isNull(oldFranchiseeUserInfo)) {
			log.error("payDeposit  ERROR! not found user! userId:{}", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0001", "未找到用户");

		}


		//判断是否缴纳押金
		if (Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
				|| Objects.isNull(oldFranchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(oldFranchiseeUserInfo.getOrderId())) {
			log.error("order  ERROR! not pay deposit! uid:{} ", oldUserInfo.getUid());
			return R.fail("ELECTRICITY.0042", "未缴纳押金");
		}


		if (!Objects.equals(oldFranchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_BATTERY) ) {
			log.error("ELECTRICITY  ERROR! not  rent battery!  userInfo:{} ", oldUserInfo);
			return R.fail("ELECTRICITY.0033", "用户未绑定电池");
		}

		ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(oldFranchiseeUserInfo.getNowElectricityBatterySn());
		if (Objects.isNull(oldElectricityBattery)) {
			log.error("webBindBattery  ERROR! not found Battery! sn:{} ", oldFranchiseeUserInfo.getNowElectricityBatterySn());
			return R.fail("ELECTRICITY.0020", "未找到电池");
		}

		//解绑电池
		FranchiseeUserInfo franchiseeUserInfo= new FranchiseeUserInfo();
		franchiseeUserInfo.setId(id);
		franchiseeUserInfo.setNowElectricityBatterySn(null);
		franchiseeUserInfo.setServiceStatus(FranchiseeUserInfo.STATUS_IS_DEPOSIT);
		franchiseeUserInfo.setUpdateTime(System.currentTimeMillis());
		Integer update = franchiseeUserInfoService.unBind(franchiseeUserInfo);

		DbUtils.dbOperateSuccessThen(update, () -> {

			//添加租电池记录
			RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
			rentBatteryOrder.setUid(oldUserInfo.getUid());
			rentBatteryOrder.setName(oldUserInfo.getName());
			rentBatteryOrder.setPhone(oldUserInfo.getPhone());
			rentBatteryOrder.setElectricityBatterySn(franchiseeUserInfo.getInitElectricityBatterySn());
			rentBatteryOrder.setBatteryDeposit(franchiseeUserInfo.getBatteryDeposit());
			rentBatteryOrder.setCreateTime(System.currentTimeMillis());
			rentBatteryOrder.setUpdateTime(System.currentTimeMillis());
			rentBatteryOrder.setType(RentBatteryOrder.TYPE_WEB_UNBIND);
			rentBatteryOrderService.insert(rentBatteryOrder);

			//修改电池状态
			ElectricityBattery electricityBattery = new ElectricityBattery();
			electricityBattery.setId(oldElectricityBattery.getId());
			electricityBattery.setStatus(ElectricityBattery.STOCK_STATUS);
			electricityBattery.setElectricityCabinetId(null);
			electricityBattery.setUid(null);
			electricityBattery.setUpdateTime(System.currentTimeMillis());
			electricityBatteryService.updateByOrder(electricityBattery);
			return null;
		});
		return R.ok();
	}

}
