package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.NewUserActivity;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.car.CarCouponNamePO;
import com.xiliulou.electricity.mapper.NewUserActivityMapper;
import com.xiliulou.electricity.query.NewUserActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.NewUserActivityPageQuery;
import com.xiliulou.electricity.query.NewUserActivityQuery;
import com.xiliulou.electricity.query.UserCouponQuery;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.NewUserActivityService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.NewUserActivityVO;
import com.xiliulou.electricity.vo.ShareAndUserActivityVO;
import com.xiliulou.electricity.vo.UserCouponVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

	
	@Resource
	private UserCouponService userCouponService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	private AssertPermissionService assertPermissionService;

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public NewUserActivity queryByIdFromCache(Integer id) {
		//先查缓存
		NewUserActivity newUserActivityCache = redisService.getWithHash(CacheConstant.NEW_USER_ACTIVITY_CACHE + id, NewUserActivity.class);
		if (Objects.nonNull(newUserActivityCache)) {
			return newUserActivityCache;
		}

		//缓存没有再查数据库
		NewUserActivity newUserActivity = newUserActivityMapper.selectById(id);
		if (Objects.isNull(newUserActivity)) {
			return null;
		}

		//放入缓存
		redisService.saveWithHash(CacheConstant.NEW_USER_ACTIVITY_CACHE + id, newUserActivity);
		return newUserActivity;
	}

	/**
	 * 新增数据
	 *
	 * @param newUserActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	public R insert(NewUserActivityAddAndUpdateQuery newUserActivityAddAndUpdateQuery) {
		//创建账号
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("Coupon  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}
		
		if (CollectionUtils.isNotEmpty(newUserActivityAddAndUpdateQuery.getCouponArrays()) && newUserActivityAddAndUpdateQuery.getCouponArrays().size() > NumberConstant.NUMBER_10){
			return R.fail("ELECTRICITY.10302", "单个奖励条件最多10张");
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
		BeanUtils.copyProperties(newUserActivityAddAndUpdateQuery, newUserActivity,IGNORE_ATTRIBUTES);
		newUserActivity.setUid(user.getUid());
		newUserActivity.setUserName(user.getUsername());
		newUserActivity.setCreateTime(System.currentTimeMillis());
		newUserActivity.setUpdateTime(System.currentTimeMillis());
		newUserActivity.setTenantId(tenantId);
		newUserActivity.setCoupons(newUserActivityAddAndUpdateQuery.getCouponArrays(),newUserActivityAddAndUpdateQuery.getCouponId());
		List<CarCouponNamePO> couponNamePOS = couponService.queryListByIdsFromCache(newUserActivityAddAndUpdateQuery.getCouponArrays());
		
		//适配单个优惠券的情况,如果有减免券,获取金额最大的减免券为单个优惠券
		Optional.ofNullable(couponNamePOS)
				.flatMap(carCouponNamePOS -> carCouponNamePOS.stream().filter(f-> Objects.equals(f.getDiscountType(), Coupon.FULL_REDUCTION)).max(Comparator.comparing(CarCouponNamePO::getAmount)))
				.ifPresent(carCouponNamePO -> newUserActivity.setCouponId(carCouponNamePO.getId().intValue()));
		//适配单个优惠券的情况,如果没有减免券,获取天数最大的天数券为单个优惠券
		Optional.ofNullable(newUserActivity.getCouponId())
				.flatMap(id -> Optional.ofNullable(couponNamePOS))
				.flatMap(carCouponNamePOS -> carCouponNamePOS.stream().filter(f-> Objects.equals(f.getDiscountType(), Coupon.DAY_VOUCHER)).max(Comparator.comparing(CarCouponNamePO::getCount)))
				.ifPresent(carCouponNamePO -> newUserActivity.setCouponId(carCouponNamePO.getId().intValue()));
		
		if (Objects.isNull(newUserActivity.getType())) {
			newUserActivity.setType(NewUserActivity.SYSTEM);
		}

		int insert = newUserActivityMapper.insert(newUserActivity);

		if (insert > 0) {
			//更新缓存
			redisService.saveWithHash(CacheConstant.NEW_USER_ACTIVITY_CACHE + newUserActivity.getId(), newUserActivity);
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
			log.error("update Activity  ERROR! not found Activity ! ActivityId={} ", newUserActivityAddAndUpdateQuery.getId());
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();
		if(!Objects.equals(oldNewUserActivity.getTenantId(),tenantId)){
			return R.ok();
		}

		//查询该租户是否有邀请活动，有则不能启用
		if (Objects.equals(newUserActivityAddAndUpdateQuery.getStatus(), NewUserActivity.STATUS_ON)) {
			int count = newUserActivityMapper.selectCount(new LambdaQueryWrapper<NewUserActivity>()
					.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));
			if (count > 0) {
				return R.fail("ELECTRICITY.00200", "该租户已有启用中的新人活动，请勿重复添加");
			}
		}

		NewUserActivity newUserActivity = new NewUserActivity();
		BeanUtil.copyProperties(newUserActivityAddAndUpdateQuery, newUserActivity,IGNORE_ATTRIBUTES);
		newUserActivity.setUpdateTime(System.currentTimeMillis());

		int update = newUserActivityMapper.updateById(newUserActivity);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.delete(CacheConstant.NEW_USER_ACTIVITY_CACHE + oldNewUserActivity.getId());
			return null;
		});

		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Slave
	@Override
	public R queryList(NewUserActivityQuery newUserActivityQuery) {
		Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
		if (!pair.getLeft()){
			return R.ok(new ArrayList<>());
		}
		newUserActivityQuery.setFranchiseeIds(pair.getRight());
		
		List<NewUserActivity> newUserActivityList = newUserActivityMapper.queryList(newUserActivityQuery);
		if (ObjectUtil.isEmpty(newUserActivityList)) {
			return R.ok(newUserActivityList);
		}

		List<NewUserActivityVO> newUserActivityVOList = new ArrayList<>();
		for (NewUserActivity newUserActivity : newUserActivityList) {
			NewUserActivityVO newUserActivityVO = new NewUserActivityVO();
			BeanUtils.copyProperties(newUserActivity, newUserActivityVO,IGNORE_ATTRIBUTES);

			if (Objects.equals(newUserActivity.getDiscountType(), NewUserActivity.TYPE_COUPON)) {
				//兼容单个优惠券的情况，先查看单个优惠券
				Integer couponId = newUserActivity.getCouponId();
				Optional.ofNullable(couponId).flatMap(id-> Optional.ofNullable(couponService.queryByIdFromCache(id)))
						.ifPresent(newUserActivityVO::setCoupon);
				//设置所有的优惠券
				List<Long> couponIds = newUserActivity.getCoupons();
//				List<Coupon> coupons = Optional.ofNullable(couponIds).orElse(List.of()).stream().map(id -> couponService.queryByIdFromCache(id.intValue())).collect(Collectors.toList());
//				Optional.of(coupons).ifPresent(newUserActivityVO::setCouponArrays);
				List<UserCouponVO> userCouponVOS = buildUserCouponVO(couponIds, newUserActivity.getTenantId());
				newUserActivityVO.setCouponArrays(userCouponVOS);
			}

			newUserActivityVOList.add(newUserActivityVO);
		}
		return R.ok(newUserActivityVOList);
	}

	@Slave
	@Override
	public R queryCount(NewUserActivityQuery newUserActivityQuery) {
		Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
		if (!pair.getLeft()){
			return R.ok(NumberConstant.ZERO);
		}
		newUserActivityQuery.setFranchiseeIds(pair.getRight());
		return R.ok(newUserActivityMapper.queryCount(newUserActivityQuery));
	}
	
	

	@Slave
	@Override
	public R queryInfo(Integer id) {
		NewUserActivity newUserActivity = queryByIdFromCache(id);
		if (Objects.isNull(newUserActivity) || !Objects.equals(newUserActivity.getTenantId(),TenantContextHolder.getTenantId())) {
			log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId={} ", id);
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
			BeanUtils.copyProperties(newUserActivity, newUserActivityVO,IGNORE_ATTRIBUTES);
			newUserActivityVO.setCoupon(coupon);
			
			
			List<UserCouponVO> userCouponVOS = buildUserCouponVO(newUserActivity.getCoupons(),newUserActivity.getTenantId());
			newUserActivityVO.setCouponArrays(userCouponVOS);

			return R.ok(newUserActivityVO);

		}

		return R.ok(newUserActivity);
	}

	@Slave
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
//			log.error("USER NOT NEW USER ! uid:{}",uid);
			return R.ok();
		}


		NewUserActivity newUserActivity = newUserActivityMapper.selectOne(new LambdaQueryWrapper<NewUserActivity>()
				.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));
		if (Objects.isNull(newUserActivity)) {
			log.info("queryInfo Activity INFO! not found Activity,tenantId={}", tenantId);
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
			BeanUtils.copyProperties(newUserActivity, newUserActivityVO,IGNORE_ATTRIBUTES);
			newUserActivityVO.setCoupon(coupon);
			
			List<UserCouponVO> userCouponVOS = buildUserCouponVO(newUserActivity.getCoupons(),tenantId);
			newUserActivityVO.setCouponArrays(userCouponVOS);
			
			return R.ok(newUserActivityVO);

		}

		return R.ok(newUserActivity);
	}

	@Override
	public NewUserActivity queryActivity() {
		Integer tenantId = TenantContextHolder.getTenantId();
        
        return newUserActivityMapper.selectOne(new LambdaQueryWrapper<NewUserActivity>()
				.eq(NewUserActivity::getTenantId, tenantId).eq(NewUserActivity::getStatus, NewUserActivity.STATUS_ON));
	}

	@Override
	public NewUserActivity selectByCouponId(Long id) {
		return newUserActivityMapper.selectByCouponId(id);
	}
	
	/**
	 * <p>
	 *    Description: delete
	 *    9. 活动管理-套餐返现活动里面的套餐配置记录想能够手动删除
	 * </p>
	 * @param id id 主键id
	 * @return com.xiliulou.core.web.R<?>
	 * <p>Project: saas-electricity</p>
	 * <p>Copyright: Copyright (c) 2024</p>
	 * <p>Company: www.xiliulou.com</p>
	 * <a herf="https://benyun.feishu.cn/wiki/GrNjwBNZkipB5wkiws2cmsEDnVU#UH1YdEuCwojVzFxtiK6c3jltneb"></a>
	 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
	 * @since V1.0 2024/3/14
	 */
    @Override
    public R<?> removeById(Long id) {
	    NewUserActivity oldNewUserActivity = queryByIdFromCache(Math.toIntExact(id));
	    if (Objects.isNull(oldNewUserActivity)) {
		    log.error("update Activity  ERROR! not found Activity ! ActivityId={} ", id);
		    return R.fail("ELECTRICITY.0069", "未找到活动");
	    }
		int count = this.newUserActivityMapper.removeById(id,TenantContextHolder.getTenantId().longValue());
		DbUtils.dbOperateSuccessThenHandleCache(Math.toIntExact(id),(identification)->{
			redisService.delete(CacheConstant.NEW_USER_ACTIVITY_CACHE + identification);
		});
        return R.ok(count);
    }
	
	@Override
	public List<ShareAndUserActivityVO> listNewUserActivity(NewUserActivityPageQuery query) {
		query.setTenantId(TenantContextHolder.getTenantId());
		return newUserActivityMapper.listNewUserActivity(query);
	}
	
	
	private List<UserCouponVO> buildUserCouponVO(List<Long> couponsIds, Integer tenantId) {
		if (CollectionUtils.isEmpty(couponsIds)) {
			return null;
		}
		UserCouponQuery query = UserCouponQuery.builder().couponIds(couponsIds).tenantId(tenantId).build();
		R r = userCouponService.queryList(query);
		if (!r.isSuccess() || Objects.isNull(r.getData())) {
			return null;
		}
		
		return(List<UserCouponVO>) r.getData();
		
	}
	
}

