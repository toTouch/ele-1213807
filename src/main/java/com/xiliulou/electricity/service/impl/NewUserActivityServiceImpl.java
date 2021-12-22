package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.OldUserActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.NewUserActivityMapper;
import com.xiliulou.electricity.query.NewUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.NewUserActivityQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.NewUserActivityVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("newUserActivityService")
@Slf4j
public class NewUserActivityServiceImpl implements NewUserActivityService {
	@Resource
	NewUserActivityMapper newUserActivityMapper;

	@Autowired
	RedisService redisService;

	@Autowired
	CouponService couponService;

	@Autowired
	UserService userService;

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public NewUserActivity queryByIdFromCache(Integer id) {
		//先查缓存
		NewUserActivity newUserActivityCache = redisService.getWithHash(ElectricityCabinetConstant.NEW_USER_ACTIVITY_CACHE + id, NewUserActivity.class);
		if (Objects.nonNull(newUserActivityCache)) {
			return newUserActivityCache;
		}

		//缓存没有再查数据库
		NewUserActivity newUserActivity = newUserActivityMapper.selectById(id);
		if (Objects.isNull(newUserActivity)) {
			return null;
		}

		//放入缓存
		redisService.saveWithHash(ElectricityCabinetConstant.NEW_USER_ACTIVITY_CACHE + id, newUserActivity);
		return newUserActivity;
	}

	/**
	 * 新增数据
	 *
	 * @param newUserActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R insert(NewUserActivityAddAndUpdateQuery newUserActivityAddAndUpdateQuery) {
		//创建账号
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("Coupon  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//查询该租户是否有新人活动，有则不能添加
		int count = newUserActivityMapper.selectCount(new LambdaQueryWrapper<NewUserActivity>()
				.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));
		if (count > 0) {
			return R.fail("ELECTRICITY.00200", "该租户已有启用中的新人活动，请勿重复添加");
		}

		NewUserActivity newUserActivity = new NewUserActivity();
		BeanUtils.copyProperties(newUserActivityAddAndUpdateQuery, newUserActivity);
		newUserActivity.setUid(user.getUid());
		newUserActivity.setUserName(user.getUsername());
		newUserActivity.setCreateTime(System.currentTimeMillis());
		newUserActivity.setUpdateTime(System.currentTimeMillis());
		newUserActivity.setTenantId(tenantId);

		if (Objects.isNull(newUserActivity.getType())) {
			newUserActivity.setType(NewUserActivity.SYSTEM);
		}

		int insert = newUserActivityMapper.insert(newUserActivity);

		DbUtils.dbOperateSuccessThen(insert, () -> {
			//更新缓存
			redisService.saveWithHash(ElectricityCabinetConstant.NEW_USER_ACTIVITY_CACHE + newUserActivity.getId(), newUserActivity);
			return null;
		});

		if (insert > 0) {
			return R.ok(newUserActivity.getId());
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	/**
	 * 修改数据(暂只支持上下架）
	 *
	 * @param newUserActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R update(NewUserActivityAddAndUpdateQuery newUserActivityAddAndUpdateQuery) {
		NewUserActivity oldNewUserActivity = queryByIdFromCache(newUserActivityAddAndUpdateQuery.getId());
		if (Objects.isNull(oldNewUserActivity)) {
			log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", newUserActivityAddAndUpdateQuery.getId());
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//查询该租户是否有邀请活动，有则不能启用
		if (Objects.equals(newUserActivityAddAndUpdateQuery.getStatus(), NewUserActivity.STATUS_ON)) {
			int count = newUserActivityMapper.selectCount(new LambdaQueryWrapper<NewUserActivity>()
					.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));
			if (count > 0) {
				return R.fail("ELECTRICITY.00200", "该租户已有启用中的新人活动，请勿重复添加");
			}
		}

		NewUserActivity newUserActivity = new NewUserActivity();
		BeanUtil.copyProperties(newUserActivityAddAndUpdateQuery, newUserActivity);
		newUserActivity.setUpdateTime(System.currentTimeMillis());

		int update = newUserActivityMapper.updateById(newUserActivity);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.delete(ElectricityCabinetConstant.NEW_USER_ACTIVITY_CACHE + oldNewUserActivity.getId());
			return null;
		});

		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	public R queryList(NewUserActivityQuery newUserActivityQuery) {
		List<NewUserActivity> newUserActivityList = newUserActivityMapper.queryList(newUserActivityQuery);
		if (ObjectUtil.isEmpty(newUserActivityList)) {
			return R.ok(newUserActivityList);
		}

		List<NewUserActivityVO> newUserActivityVOList = new ArrayList<>();
		for (NewUserActivity newUserActivity : newUserActivityList) {
			NewUserActivityVO newUserActivityVO = new NewUserActivityVO();
			BeanUtils.copyProperties(newUserActivity, newUserActivityVO);

			if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON)) {
				if (Objects.isNull(newUserActivity.getCouponId())) {
					continue;
				}

				Coupon coupon = couponService.queryByIdFromCache(newUserActivity.getCouponId());
				if (Objects.isNull(coupon)) {
					log.error("queryInfo Activity  ERROR! not found coupon ! couponId:{} ", newUserActivity.getCouponId());
					continue;
				}

				newUserActivityVO.setCoupon(coupon);
			}
			newUserActivityVOList.add(newUserActivityVO);

		}
		return R.ok(newUserActivityVOList);

	}

	@Override
	public R queryCount(NewUserActivityQuery newUserActivityQuery) {
		return R.ok(newUserActivityMapper.queryCount(newUserActivityQuery));
	}

	@Override
	public R queryInfo(Integer id) {
		NewUserActivity newUserActivity = queryByIdFromCache(id);
		if (Objects.isNull(newUserActivity)) {
			log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON)) {
			if (Objects.isNull(newUserActivity.getCouponId())) {
				return R.ok(newUserActivity);
			}

			Coupon coupon = couponService.queryByIdFromCache(newUserActivity.getCouponId());
			if (Objects.isNull(coupon)) {
				log.error("queryInfo Activity  ERROR! not found coupon ! couponId:{} ", newUserActivity.getCouponId());
				return R.ok(newUserActivity);
			}

			NewUserActivityVO newUserActivityVO = new NewUserActivityVO();
			BeanUtils.copyProperties(newUserActivity, newUserActivityVO);
			newUserActivityVO.setCoupon(coupon);

			return R.ok(newUserActivityVO);

		}

		return R.ok(newUserActivity);
	}

	@Override
	public R queryNewUserActivity() {
		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户
		Long uid = SecurityUtils.getUid();
		if (Objects.isNull(uid)) {
			log.error("queryNewUserActivity  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}


		//查询用户注册时间，超过一分钟非注册登录则不弹出
		User user=userService.queryByUidFromCache(uid);
		if (Objects.isNull(user)) {
			log.error("queryNewUserActivity  ERROR! not found user ! uid:{}",uid);
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}


		if(user.getCreateTime()+60*1000L<System.currentTimeMillis()){
			log.error("USER NOT NEW USER ! uid:{}",uid);
			return R.ok();
		}


		NewUserActivity newUserActivity = newUserActivityMapper.selectOne(new LambdaQueryWrapper<NewUserActivity>()
				.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));
		if (Objects.isNull(newUserActivity)) {
			log.error("queryInfo Activity  ERROR! not found Activity !  tenantId:{} ", tenantId);
			return R.ok();
		}


		if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON)) {
			if (Objects.isNull(newUserActivity.getCouponId())) {
				return R.ok(newUserActivity);
			}

			Coupon coupon = couponService.queryByIdFromCache(newUserActivity.getCouponId());
			if (Objects.isNull(coupon)) {
				log.error("queryInfo Activity  ERROR! not found coupon ! couponId:{} ", newUserActivity.getCouponId());
				return R.ok(newUserActivity);
			}

			NewUserActivityVO newUserActivityVO = new NewUserActivityVO();
			BeanUtils.copyProperties(newUserActivity, newUserActivityVO);
			newUserActivityVO.setCoupon(coupon);

			return R.ok(newUserActivityVO);

		}

		return R.ok(newUserActivity);
	}

	@Override
	public NewUserActivity queryActivity() {
		Integer tenantId = TenantContextHolder.getTenantId();

		NewUserActivity newUserActivity = newUserActivityMapper.selectOne(new LambdaQueryWrapper<NewUserActivity>()
				.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));

		return newUserActivity;
	}

}

