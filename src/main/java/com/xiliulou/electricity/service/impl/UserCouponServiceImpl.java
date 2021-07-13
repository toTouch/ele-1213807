package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserCouponMapper;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券表(TCoupon)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:59
 */
@Service("userCouponService")
@Slf4j
public class UserCouponServiceImpl implements UserCouponService {
	@Resource
	private UserCouponMapper userCouponMapper;
	@Autowired
	private CouponService couponService;

	@Autowired
	UserService userService;

	@Autowired
	UserInfoService userInfoService;

	@Autowired
	FranchiseeService franchiseeService;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public R queryList(UserCouponQuery userCouponQuery) {
		List<UserCoupon> userCouponList = userCouponMapper.queryList(userCouponQuery);
		return R.ok(userCouponList);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R batchRelease(Integer id, Long[] uidS) {
		if (ObjectUtil.isEmpty(uidS)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		Coupon oldCoupon = couponService.queryByIdFromCache(id);
		if (Objects.isNull(oldCoupon)) {
			log.error("Coupon  ERROR! not found coupon ! couponId:{} ", id);
			return R.fail("ELECTRICITY.0085", "找不到优惠券");
		}

		UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
				.name(oldCoupon.getName())
				.source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
				.couponId(oldCoupon.getId())
				.discountType(oldCoupon.getDiscountType())
				.status(UserCoupon.STATUS_UNUSED)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis());

		//优惠券过期时间

		LocalDateTime now = LocalDateTime.now().plusDays(oldCoupon.getDays());
		couponBuild.deadline(TimeUtils.convertTimeStamp(now));

		//批量插入
		for (Long uid : uidS) {
			//查询用户手机号
			User user = userService.queryByUidFromCache(uid);
			if (Objects.isNull(user)) {
				return R.fail("ELECTRICITY.0019", "未找到用户");
			}
			couponBuild.uid(uid);
			couponBuild.phone(user.getPhone());
			UserCoupon userCoupon = couponBuild.build();
			userCouponMapper.insert(userCoupon);
		}

		return R.ok();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void handelUserCouponExpired() {
		//分页只修改200条
		List<UserCoupon> userCouponList = userCouponMapper.getExpiredUserCoupon(System.currentTimeMillis(), 0, 200);
		if (!DataUtil.collectionIsUsable(userCouponList)) {
			return;
		}
		for (UserCoupon userCoupon : userCouponList) {
			userCoupon.setStatus(UserCoupon.STATUS_EXPIRED);
			userCoupon.setUpdateTime(System.currentTimeMillis());
			userCouponMapper.updateById(userCoupon);
		}
	}

	@Override
	public R queryMyCoupon(List<Integer> statusList, List<Integer> typeList) {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//2.判断用户
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		if (Objects.isNull(userInfo)) {
			log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0019", "未找到用户");
		}
		//用户是否可用
		if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! user is unusable!uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//未实名认证
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("ELECTRICITY  ERROR! not auth! uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//查看用户是否参与过活动
		UserCouponQuery userCouponQuery = new UserCouponQuery();
		userCouponQuery.setStatusList(statusList);
		userCouponQuery.setUid(uid);
		userCouponQuery.setTypeList(typeList);
		List<UserCoupon> userCouponList = userCouponMapper.queryList(userCouponQuery);
		return R.ok(userCouponList);
	}

	/*

	 1、判断优惠券是否上架
	 2、判断用户是否可以领取优惠券
	 3、领取优惠券
	 4、优惠券领取成功或失败

	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R getCoupon(List<Integer> couponIdList, Integer id, Integer type) {
		//用户信息
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		User user = userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(uid);
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", uid);
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}


		return R.ok();
	}
}
