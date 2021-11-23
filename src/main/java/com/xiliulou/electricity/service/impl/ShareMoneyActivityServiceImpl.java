package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareMoneyActivity;
import com.xiliulou.electricity.mapper.ShareMoneyActivityMapper;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityAddAndUpdateQuery;
import com.xiliulou.electricity.query.ShareMoneyActivityQuery;
import com.xiliulou.electricity.service.ShareMoneyActivityService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 活动表(TActivity)表服务实现类
 *
 * @author makejava
 * @since 2021-04-14 09:27:12
 */
@Service("shareMoneyActivityService")
@Slf4j
public class ShareMoneyActivityServiceImpl implements ShareMoneyActivityService {
	@Resource
	private ShareMoneyActivityMapper shareMoneyActivityMapper;

	@Autowired
	RedisService redisService;


	/**
	 * 通过ID查询单条数据从缓存
	 *
	 * @param id 主键
	 * @return 实例对象
	 */
	@Override
	public ShareMoneyActivity queryByIdFromCache(Integer id) {
		//先查缓存
		ShareMoneyActivity shareMoneyActivityCache = redisService.getWithHash(ElectricityCabinetConstant.SHARE_MONEY_ACTIVITY_CACHE + id, ShareMoneyActivity.class);
		if (Objects.nonNull(shareMoneyActivityCache)) {
			return shareMoneyActivityCache;
		}

		//缓存没有再查数据库
		ShareMoneyActivity shareMoneyActivity = shareMoneyActivityMapper.selectById(id);
		if (Objects.isNull(shareMoneyActivity)) {
			return null;
		}

		//放入缓存
		redisService.saveWithHash(ElectricityCabinetConstant.SHARE_MONEY_ACTIVITY_CACHE + id, shareMoneyActivity);
		return shareMoneyActivity;
	}

	/**
	 * 新增数据
	 *
	 * @param shareMoneyActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R insert(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
		//创建账号
		TokenUser user = SecurityUtils.getUserInfo();
		if (Objects.isNull(user)) {
			log.error("Coupon  ERROR! not found user ");
			return R.fail("ELECTRICITY.0001", "未找到用户");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//查询该租户是否有邀请活动，有则不能添加
		int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>()
				.eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
		if (count > 0) {
			return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
		}


		ShareMoneyActivity shareMoneyActivity = new ShareMoneyActivity();
		BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
		shareMoneyActivity.setUid(user.getUid());
		shareMoneyActivity.setUserName(user.getUsername());
		shareMoneyActivity.setCreateTime(System.currentTimeMillis());
		shareMoneyActivity.setUpdateTime(System.currentTimeMillis());
		shareMoneyActivity.setTenantId(tenantId);

		if (Objects.isNull(shareMoneyActivity.getType())) {
			shareMoneyActivity.setType(ShareActivity.SYSTEM);
		}

		int insert = shareMoneyActivityMapper.insert(shareMoneyActivity);
		DbUtils.dbOperateSuccessThen(insert, () -> {
			//放入缓存
			redisService.saveWithHash(ElectricityCabinetConstant.SHARE_MONEY_ACTIVITY_CACHE + shareMoneyActivity.getId(), shareMoneyActivity);
			return null;
		});

		if (insert > 0) {
			return R.ok(shareMoneyActivity.getId());
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	/**
	 * 修改数据(暂只支持上下架）
	 *
	 * @param shareMoneyActivityAddAndUpdateQuery 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public R update(ShareMoneyActivityAddAndUpdateQuery shareMoneyActivityAddAndUpdateQuery) {
		ShareMoneyActivity oldShareMoneyActivity = queryByIdFromCache(shareMoneyActivityAddAndUpdateQuery.getId());
		if (Objects.isNull(oldShareMoneyActivity)) {
			log.error("update Activity  ERROR! not found Activity ! ActivityId:{} ", shareMoneyActivityAddAndUpdateQuery.getId());
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		//租户
		Integer tenantId = TenantContextHolder.getTenantId();

		//查询该租户是否有邀请活动，有则不能启用
		if (Objects.equals(shareMoneyActivityAddAndUpdateQuery.getStatus(), ShareMoneyActivity.STATUS_ON)) {
			int count = shareMoneyActivityMapper.selectCount(new LambdaQueryWrapper<ShareMoneyActivity>()
					.eq(ShareMoneyActivity::getTenantId, tenantId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
			if (count > 0) {
				return R.fail("ELECTRICITY.00102", "该租户已有启用中的邀请活动，请勿重复添加");
			}
		}

		ShareMoneyActivity shareMoneyActivity=new ShareMoneyActivity();
		BeanUtil.copyProperties(shareMoneyActivityAddAndUpdateQuery, shareMoneyActivity);
		shareMoneyActivity.setUpdateTime(System.currentTimeMillis());

		int update = shareMoneyActivityMapper.updateById(shareMoneyActivity);
		DbUtils.dbOperateSuccessThen(update, () -> {
			//更新缓存
			redisService.delete(ElectricityCabinetConstant.SHARE_ACTIVITY_CACHE + oldShareMoneyActivity.getId());
			return null;
		});

		if (update > 0) {
			return R.ok();
		}
		return R.fail("ELECTRICITY.0086", "操作失败");
	}

	@Override
	public R queryList(ShareMoneyActivityQuery shareMoneyActivityQuery) {
		List<ShareMoneyActivity> shareMoneyActivityList = shareMoneyActivityMapper.queryList(shareMoneyActivityQuery);
		return R.ok(shareMoneyActivityList);
	}


	@Override
	public R queryCount(ShareMoneyActivityQuery shareMoneyActivityQuery) {
		Integer count = shareMoneyActivityMapper.queryCount(shareMoneyActivityQuery);
		return R.ok(count);
	}


	@Override
	public R queryInfo(Integer id) {
		ShareMoneyActivity shareMoneyActivity = queryByIdFromCache(id);
		if (Objects.isNull(shareMoneyActivity)) {
			log.error("queryInfo Activity  ERROR! not found Activity ! ActivityId:{} ", id);
			return R.fail("ELECTRICITY.0069", "未找到活动");
		}

		return R.ok(shareMoneyActivity);
	}


	@Override
	public ShareMoneyActivity queryByStatus(Integer activityId) {
		return shareMoneyActivityMapper.selectOne(new LambdaQueryWrapper<ShareMoneyActivity>()
				.eq(ShareMoneyActivity::getId, activityId).eq(ShareMoneyActivity::getStatus, ShareMoneyActivity.STATUS_ON));
	}



}

