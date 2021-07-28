package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.JoinShareActivityHistory;
import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.entity.ShareActivityRule;
import com.xiliulou.electricity.entity.Coupon;
import com.xiliulou.electricity.entity.UserCoupon;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ShareActivityMapper;
import com.xiliulou.electricity.query.ShareActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.query.ShareActivityRuleQuery;
import com.xiliulou.electricity.service.JoinShareActivityHistoryService;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityRecordService;
import com.xiliulou.electricity.service.ShareActivityRuleService;
import com.xiliulou.electricity.service.ShareActivityService;
import com.xiliulou.electricity.service.CouponService;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserCouponService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ShareActivityVO;
import com.xiliulou.electricity.vo.CouponVO;
import com.xiliulou.security.bean.TokenUser;
import com.xiliulou.storage.config.StorageConfig;
import com.xiliulou.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Service("shareActivityService")
@Slf4j
public class ShareActivityServiceImpl implements ShareActivityService {
	@Resource
	private ShareActivityMapper shareActivityMapper;

	@Autowired
	RedisService redisService;

	@Autowired
	UserInfoService userInfoService;

	@Autowired
	FranchiseeService franchiseeService;

	@Autowired
	UserService userService;

	@Autowired
	private ShareActivityRuleService shareActivityRuleService;

	@Autowired
	private CouponService couponService;

	@Autowired
	ElectricityCabinetFileService electricityCabinetFileService;

	@Autowired
	StorageConfig storageConfig;

	@Qualifier("aliyunOssService")
	@Autowired
	StorageService storageService;

	@Autowired
	ShareActivityRecordService shareActivityRecordService;

	@Autowired
	UserCouponService userCouponService;

	@Autowired
	JoinShareActivityRecordService joinShareActivityRecordService;

	@Autowired
	JoinShareActivityHistoryService joinShareActivityHistoryService;

	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ShareActivity queryByIdFromCache(Integer id) {
		//先查缓存
		ShareActivity shareActivityCache = redisService.getWithHash(ElectricityCabinetConstant.SHARE_ACTIVITY_CACHE + id, ShareActivity.class);
		if (Objects.nonNull(shareActivityCache)) {
			return shareActivityCache;
		}

		//缓存没有再查数据库
		ShareActivity shareActivity = shareActivityMapper.selectById(id);
		if (Objects.isNull(shareActivity)) {
			return null;
		}

		//放入缓存
		redisService.saveWithHash(ElectricityCabinetConstant.SHARE_ACTIVITY_CACHE + id, shareActivity);
		return shareActivity;
	}

	/**
	 * 新增数据
	 *
	 * @param shareActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R insert(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
		//创建账号
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("Coupon  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//查询该租户是否有邀请活动，有则不能添加
		int count = shareActivityMapper.selectCount(new LambdaQueryWrapper<ShareActivity>()
				.eq(ShareActivity::getTenantId, tenantId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
		if (count > 0) {
			return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
		}

		List<ShareActivityRuleQuery> shareActivityRuleQueryList = shareActivityAddAndUpdateQuery.getShareActivityRuleQueryList();

		ShareActivity shareActivity = new ShareActivity();
		BeanUtil.copyProperties(shareActivityAddAndUpdateQuery, shareActivity);
		shareActivity.setUid(user.getUid());
		shareActivity.setUserName(user.getUsername());
		shareActivity.setCreateTime(System.currentTimeMillis());
		shareActivity.setUpdateTime(System.currentTimeMillis());
		shareActivity.setTenantId(tenantId);

		if (Objects.isNull(shareActivity.getType())) {
			shareActivity.setType(ShareActivity.SYSTEM);
		}

		int insert = shareActivityMapper.insert(shareActivity);
		DbUtils.dbOperateSuccessThen(insert, () -> {

			//添加优惠
			if (ObjectUtil.isNotEmpty(shareActivityRuleQueryList)) {
				for (ShareActivityRuleQuery shareActivityRuleQuery : shareActivityRuleQueryList) {
					ShareActivityRule.ShareActivityRuleBuilder activityBindCouponBuild = ShareActivityRule.builder()
							.activityId(shareActivity.getId())
							.couponId(shareActivityRuleQuery.getCouponId())
							.triggerCount(shareActivityRuleQuery.getTriggerCount())
							.createTime(System.currentTimeMillis())
							.updateTime(System.currentTimeMillis());
					ShareActivityRule shareActivityRule = activityBindCouponBuild.build();
					shareActivityRuleService.insert(shareActivityRule);
				}
			}

			return null;
		});

		if (insert > 0) {
			return R.ok(shareActivity.getId());
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	/**
	 * 修改数据(暂只支持上下架）
	 *
	 * @param shareActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R update(ShareActivityAddAndUpdateQuery shareActivityAddAndUpdateQuery) {
		ShareActivity oldShareActivity = queryByIdFromCache(shareActivityAddAndUpdateQuery.getId());
		if (Objects.isNull(oldShareActivity)) {
			log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", shareActivityAddAndUpdateQuery.getId());
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//查询该租户是否有邀请活动，有则不能启用
		if (Objects.equals(shareActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_ON)) {
			int count = shareActivityMapper.selectCount(new LambdaQueryWrapper<ShareActivity>()
					.eq(ShareActivity::getTenantId, tenantId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
			if (count > 0) {
				return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
			}
		}

		BeanUtil.copyProperties(shareActivityAddAndUpdateQuery, oldShareActivity);
		oldShareActivity.setUpdateTime(System.currentTimeMillis());

		int update = shareActivityMapper.updateById(oldShareActivity);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.saveWithHash(ElectricityCabinetConstant.SHARE_ACTIVITY_CACHE + oldShareActivity.getId(), oldShareActivity);

			//如果是下架活动，则参与邀请记录改为活动已下架
			if (Objects.equals(shareActivityAddAndUpdateQuery.getStatus(), ShareActivity.STATUS_OFF)) {

				//修改邀请状态
				JoinShareActivityRecord joinShareActivityRecord = new JoinShareActivityRecord();
				joinShareActivityRecord.setStatus(JoinShareActivityRecord.STATUS_OFF);
				joinShareActivityRecord.setUpdateTime(System.currentTimeMillis());
				joinShareActivityRecord.setActivityId(shareActivityAddAndUpdateQuery.getId());
				joinShareActivityRecordService.updateByActivityId(joinShareActivityRecord);

				//修改历史记录状态
				JoinShareActivityHistory joinShareActivityHistory = new JoinShareActivityHistory();
				joinShareActivityHistory.setStatus(JoinShareActivityHistory.STATUS_OFF);
				joinShareActivityHistory.setUpdateTime(System.currentTimeMillis());
				joinShareActivityHistory.setActivityId(shareActivityAddAndUpdateQuery.getId());
				joinShareActivityHistoryService.updateByActivityId(joinShareActivityHistory);

			}
			return null;
		});

		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	public R queryList(ShareActivityQuery shareActivityQuery) {
		List<ShareActivity> shareActivityList = shareActivityMapper.queryList(shareActivityQuery);
		return R.ok(shareActivityList);
	}

	@Override
	public R queryInfo(Integer id) {
		ShareActivity shareActivity = queryByIdFromCache(id);
		if (Objects.isNull(shareActivity)) {
			log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		ShareActivityVO shareActivityVO = new ShareActivityVO();
		BeanUtil.copyProperties(shareActivity, shareActivityVO);

		//小活动
		getCouponVOList(shareActivityVO);
		return R.ok(shareActivityVO);
	}

	private void getCouponVOList(ShareActivityVO shareActivityVO) {
		List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(shareActivityVO.getId());
		if (ObjectUtil.isEmpty(shareActivityRuleList)) {
			return;
		}

		List<CouponVO> couponVOList = new ArrayList<>();
		for (ShareActivityRule shareActivityRule : shareActivityRuleList) {
			CouponVO couponVO = new CouponVO();
			couponVO.setTriggerCount(shareActivityRule.getTriggerCount());
			Integer couponId = shareActivityRule.getCouponId();
			//优惠券名称
			Coupon coupon = couponService.queryByIdFromCache(couponId);
			if (Objects.nonNull(coupon)) {
				couponVO.setCoupon(coupon);
			}
			couponVOList.add(couponVO);
		}

		shareActivityVO.setCouponVOList(couponVOList);
	}

	private void getUserCouponVOList(ShareActivityVO shareActivityVO, TokenUser user) {
		List<ShareActivityRule> shareActivityRuleList = shareActivityRuleService.queryByActivity(shareActivityVO.getId());
		if (ObjectUtil.isEmpty(shareActivityRuleList)) {
			return;
		}

		//邀请好友数
		int count = 0;
		//可用邀请好友数
		int availableCount = 0;
		ShareActivityRecord shareActivityRecord = shareActivityRecordService.queryByUid(user.getUid(), shareActivityVO.getId());
		if (Objects.nonNull(shareActivityRecord)) {
			count = shareActivityRecord.getCount();
			availableCount = shareActivityRecord.getAvailableCount();
		}

		//
		List<CouponVO> couponVOList = new ArrayList<>();
		int couponCount = 0;
		for (ShareActivityRule shareActivityRule : shareActivityRuleList) {

			CouponVO couponVO = new CouponVO();
			couponVO.setTriggerCount(shareActivityRule.getTriggerCount());
			Integer couponId = shareActivityRule.getCouponId();

			//是否可以领取优惠券
			if (shareActivityRule.getTriggerCount() <= availableCount) {
				couponVO.setIsGet(CouponVO.IS_NOT_RECEIVE);
			} else {
				couponVO.setIsGet(CouponVO.IS_CANNOT_RECEIVE);
			}

			//优惠券名称
			Coupon coupon = couponService.queryByIdFromCache(couponId);
			if (Objects.nonNull(coupon)) {

				//是否领取该活动该优惠券
				UserCoupon userCoupon = userCouponService.queryByActivityIdAndCouponId(shareActivityVO.getId(), coupon.getId(),user.getUid());
				if (Objects.nonNull(userCoupon)) {
					couponVO.setIsGet(CouponVO.IS_RECEIVED);
					couponCount = couponCount + 1;
				}
				couponVO.setCoupon(coupon);
			}
			couponVOList.add(couponVO);
		}

		//邀请好友数
		shareActivityVO.setCount(count);
		//可用邀请好友数
		shareActivityVO.setAvailableCount(availableCount);

		//领卷次数
		shareActivityVO.setCouponCount(couponCount);

		shareActivityVO.setCouponVOList(couponVOList);
	}

	@Override
	public R queryCount(ShareActivityQuery shareActivityQuery) {
		Integer count = shareActivityMapper.queryCount(shareActivityQuery);
		return R.ok(count);
	}

	@Override
	public R activityInfo() {
		//用户
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("order  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//用户是否可用
		UserInfo userInfo = userInfoService.queryByUid(user.getUid());
		if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
			log.error("ELECTRICITY  ERROR! not found userInfo,uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0024", "用户已被禁用");
		}

		//邀请活动
		ShareActivity shareActivity = shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>()
				.eq(ShareActivity::getTenantId, tenantId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
		if (Objects.isNull(shareActivity)) {
			log.error("queryInfo Activity  ERROR! not found Activity ! tenantId:{} ", tenantId);
			return R.ok();
		}

		//未实名认证
		if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
			log.error("order  ERROR! user not auth!  uid:{} ", user.getUid());
			return R.fail("ELECTRICITY.0041", "未实名认证");
		}

		ShareActivityVO shareActivityVO = new ShareActivityVO();
		BeanUtil.copyProperties(shareActivity, shareActivityVO);

		//优惠券
		getUserCouponVOList(shareActivityVO, user);

		return R.ok(shareActivityVO);

	}

	@Override
	public ShareActivity queryByStatus(Integer activityId) {
		return shareActivityMapper.selectOne(new LambdaQueryWrapper<ShareActivity>()
				.eq(ShareActivity::getId, activityId).eq(ShareActivity::getStatus, ShareActivity.STATUS_ON));
	}

}

