package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivityRule;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserCouponMapper;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityRuleService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserCouponVO;
import com.xiliulou.security.bean.TokenUser;
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
	ShareActivityRecordService shareActivityRecordService;

	@Autowired
	ShareActivityService shareActivityService;

	@Autowired
	ShareActivityRuleService shareActivityRuleService;

	@Override
	public R queryList(UserCouponQuery userCouponQuery) {
		List<UserCoupon> userCouponList = userCouponMapper.queryList(userCouponQuery);
		return R.ok(userCouponList);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R batchRelease(Integer id, Long[] uids) {
		if (ObjectUtil.isEmpty(uids)) {
			return R.fail("ELECTRICITY.0007", "不合法的参数");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		Coupon coupon = couponService.queryByIdFromCache(id);
		if (Objects.isNull(coupon)) {
			log.error("Coupon  ERROR! not found coupon ! couponId:{} ", id);
			return R.fail("ELECTRICITY.0085", "未找到优惠券");
		}

		UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
				.name(coupon.getName())
				.source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
				.couponId(coupon.getId())
				.discountType(coupon.getDiscountType())
				.status(UserCoupon.STATUS_UNUSED)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis())
				.tenantId(tenantId);

		//优惠券过期时间

		LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
		couponBuild.deadline(TimeUtils.convertTimeStamp(now));

		//批量插入
		for (Long uid : uids) {
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

		//查看用户优惠券
		UserCouponQuery userCouponQuery = new UserCouponQuery();
		userCouponQuery.setStatusList(statusList);
		userCouponQuery.setUid(uid);
		userCouponQuery.setTypeList(typeList);
		List<UserCoupon> userCouponList = userCouponMapper.queryList(userCouponQuery);

		if(Objects.isNull(userCouponList)){
			return R.ok(userCouponList);
		}

		List<UserCouponVO> userCouponVOList=new ArrayList<>();
		for (UserCoupon userCoupon:userCouponList) {

			UserCouponVO userCouponVO=new UserCouponVO();
			BeanUtil.copyProperties(userCoupon,userCouponVO);

			Coupon coupon=couponService.queryByIdFromCache(userCoupon.getCouponId());
			if(Objects.nonNull(coupon)){
				userCouponVO.setAmount(coupon.getAmount());
				userCouponVO.setDiscount(coupon.getDiscount());
			}

			userCouponVOList.add(userCouponVO);
		}

		return R.ok(userCouponVOList);

	}

	/*

	 1、判断优惠券是否上架
	 2、判断用户是否可以领取优惠券
	 3、领取优惠券
	 4、优惠券领取成功或失败

	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R getShareCoupon(Integer activityId, Integer couponId) {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("getShareCoupon  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//判断是否实名认证
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		//用户是否可用
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("getShareCoupon  ERROR! not found userInfo,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		//查找活动
		ShareActivity shareActivity = shareActivityService.queryByIdFromCache(activityId);
		if (Objects.isNull(shareActivity)) {
			log.error("getShareCoupon  ERROR! not found Activity ! ActivityId:{} ", activityId);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//查询活动规则
		List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(activityId);
		if (ObjectUtil.isEmpty(shareActivityRuleList)) {
			log.error("getShareCoupon ERROR! not found Activity ! ActivityId:{} ", activityId);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		UserCoupon oldUserCoupon=queryByActivityIdAndCouponId(activityId,couponId);
		if(Objects.nonNull(oldUserCoupon)){
			return R.fail("ELECTRICITY.00104", "已领过该张优惠券，请不要贪心哦");
		}

		//判断用户是否可以领取优惠券
		ShareActivityRecord shareActivityRecord = shareActivityRecordService.queryByUid(user.getUid());
		if (Objects.isNull(shareActivityRecord)) {
			return R.fail("ELECTRICITY.00103", "该用户邀请好友不够，领劵失败");
		}

		//查询优惠券是否在活动中间
		for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
			if (Objects.equals(shareActivityRule.getCouponId(), couponId)) {
				if (shareActivityRecord.getAvailableCount() < shareActivityRule.getTriggerCount()) {
					return R.fail("ELECTRICITY.00103", "该用户邀请好友不够，领劵失败");
				} else {
					//领劵
					Coupon coupon = couponService.queryByIdFromCache(couponId);
					if (Objects.isNull(coupon)) {
						log.error("getShareCoupon  ERROR! not found coupon ! couponId:{} ", couponId);
						return R.fail("ELECTRICITY.0085", "未找到优惠券");
					}

					LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
					UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
							.name(coupon.getName())
							.source(UserCoupon.TYPE_SOURCE_ADMIN_SEND)
							.activityId(activityId)
							.couponId(couponId)
							.discountType(coupon.getDiscountType())
							.status(UserCoupon.STATUS_UNUSED)
							.createTime(System.currentTimeMillis())
							.updateTime(System.currentTimeMillis())
							.uid(user.getUid())
							.phone(user.getPhone())
							.deadline(TimeUtils.convertTimeStamp(now))
							.tenantId(tenantId);

					UserCoupon userCoupon = couponBuild.build();
					userCouponMapper.insert(userCoupon);

					//领劵完，可用邀请人数减少
					shareActivityRecordService.reduceAvailableCountByUid(user.getUid(),shareActivityRule.getTriggerCount());
					return R.ok("领取成功");
				}
			}
		}

		return R.fail("ELECTRICITY.0085", "未找到优惠券");

	}

	@Override
	public UserCoupon queryByIdFromDB(Integer userCouponId) {
		return userCouponMapper.selectById(userCouponId);
	}

	@Override
	public UserCoupon queryByActivityIdAndCouponId(Integer activityId, Integer couponId) {
		return userCouponMapper.selectOne(new LambdaQueryWrapper<UserCoupon>()
				.eq(UserCoupon::getActivityId,activityId).eq(UserCoupon::getCouponId,couponId));
	}

	@Override
	public void update(UserCoupon userCoupon) {
		userCouponMapper.updateById(userCoupon);
	}
}
