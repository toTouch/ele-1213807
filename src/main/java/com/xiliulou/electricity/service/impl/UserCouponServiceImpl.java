package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.Activity;
import com.xiliulou.electricity.entity.ActivityBindCoupon;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.UserCouponMapper;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.ActivityBindCouponService;
import com.xiliulou.electricity.service.ActivityService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.UserCouponVO;
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
	private ActivityBindCouponService activityBindCouponService;

	@Autowired
	ActivityService activityService;

	@Autowired
	FranchiseeService franchiseeService;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 通过ID查询单条数据从DB
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public UserCoupon queryByIdFromDB(Long id) {
		return this.userCouponMapper.selectById(id);
	}

	/**
	 * 新增数据
	 *
	 * @param userCoupon 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public UserCoupon insert(UserCoupon userCoupon) {
		this.userCouponMapper.insert(userCoupon);
		return userCoupon;
	}

	/**
	 * 修改数据
	 *
	 * @param userCoupon 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer update(UserCoupon userCoupon) {
		return this.userCouponMapper.updateById(userCoupon);

	}

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
		if (Objects.equals(oldCoupon.getTimeType(), Coupon.TYPE_TIME_DAY)) {
			LocalDateTime now = LocalDateTime.now().plusDays(oldCoupon.getDays());
			couponBuild.deadline(TimeUtils.convertTimeStamp(now));
		} else {
			couponBuild.deadline(oldCoupon.getEndTime());
		}

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
		if (ObjectUtil.isEmpty(userCouponList)) {
			return R.ok();
		}
		List<UserCouponVO> userCouponVOList = new ArrayList<>();
		for (UserCoupon userCoupon : userCouponList) {
			UserCouponVO userCouponVO = new UserCouponVO();
			BeanUtil.copyProperties(userCoupon, userCouponVO);

			Coupon coupon = couponService.queryByIdFromCache(userCoupon.getCouponId());
			if (Objects.nonNull(coupon)) {
				userCouponVO.setAmount(coupon.getAmount());
				userCouponVO.setDiscount(coupon.getDiscount());
				userCouponVO.setTimeType(coupon.getTimeType());
				userCouponVO.setStartTime(coupon.getStartTime());
				userCouponVO.setEndTime(coupon.getEndTime());
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

		if (Objects.equals(type, Activity.SYSTEM_OUT_URL) || Objects.equals(type, Activity.FRANCHISEE_OUT_URL)) {
			log.error("getCoupon Activity  ERROR! out url not get coupon ! type:{} ", type);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//判断优惠券是否可以领取
		if (ObjectUtil.isEmpty(couponIdList)) {
			return R.ok();
		}

		//查活动
		Activity activity = activityService.queryByIdFromCache(id);

		if (Objects.isNull(activity) || Objects.equals(activity.getStatus(), Activity.STATUS_OFF)) {
			log.error("getCoupon Activity  ERROR! not found Activity ! ActivityId:{} ", id);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//活动领取次数
		Long receiveCount = (Long) redisTemplate.opsForValue().get(ElectricityCabinetConstant.CACHE_RECEIVE_ACTIVITY + id);
		if (Objects.isNull(receiveCount)) {
			receiveCount = 0L;
		}
		redisTemplate.opsForValue().set(ElectricityCabinetConstant.CACHE_LOOK_ACTIVITY + id, receiveCount + 1L);

		UserCoupon.UserCouponBuilder couponBuild = UserCoupon.builder()
				.uid(uid)
				.phone(user.getPhone())
				.activityId(id)
				.source(UserCoupon.TYPE_SOURCE_ACTIVITY)
				.status(UserCoupon.STATUS_UNUSED)
				.createTime(System.currentTimeMillis())
				.updateTime(System.currentTimeMillis());

		for (Integer couponId : couponIdList) {
			//查优惠券
			Coupon coupon = couponService.queryByIdFromCache(couponId);
			if (Objects.isNull(coupon)) {
				log.error("getCoupon Activity  ERROR! not found coupon ! couponId:{} ", couponId);
				if (couponIdList.size() <= 1) {
					return R.fail("ELECTRICITY.0085", "找不到优惠券");
				}
				continue;
			}

			//优惠券限领次数
			Integer count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUid, uid).eq(UserCoupon::getSource, UserCoupon.TYPE_SOURCE_ACTIVITY).eq(UserCoupon::getCouponId, couponId));
			if (count >= coupon.getMaxTimesEveryone()) {
				log.error("getCoupon Activity  ERROR! count is bigger MaxTimesEveryone  count:{} ", count);
				if (couponIdList.size() <= 1) {
					return R.fail("ELECTRICITY.0084", "该优惠劵已达到最大领取次数");
				}
				continue;
			}

			ActivityBindCoupon activityBindCoupon = activityBindCouponService.queryByCouponId(id, couponId);
			if (Objects.isNull(activityBindCoupon)) {
				log.error("getCoupon Activity  ERROR! not found activityBindCoupon ! couponId:{} ", couponId);
				if (couponIdList.size() <= 1) {
					return R.fail("ELECTRICITY.0085", "找不到优惠券");
				}
				continue;
			}
			couponBuild.couponId(couponId);
			couponBuild.name(coupon.getName());
			couponBuild.discountType(coupon.getDiscountType());

			//优惠券过期时间
			if (Objects.equals(coupon.getTimeType(), Coupon.TYPE_TIME_DAY)) {
				LocalDateTime now = LocalDateTime.now().plusDays(coupon.getDays());
				couponBuild.deadline(TimeUtils.convertTimeStamp(now));
			} else {
				couponBuild.deadline(coupon.getEndTime());
			}
			UserCoupon userCoupon = couponBuild.build();
			userCouponMapper.insert(userCoupon);

			//优惠券数量减少
			activityBindCoupon.setCouponCount(activityBindCoupon.getCouponCount() - 1);
			activityBindCoupon.setReceiveCount(activityBindCoupon.getReceiveCount() + 1);
			activityBindCoupon.setUpdateTime(System.currentTimeMillis());
			activityBindCouponService.update(activityBindCoupon);

		}

		return R.ok();
	}
}
