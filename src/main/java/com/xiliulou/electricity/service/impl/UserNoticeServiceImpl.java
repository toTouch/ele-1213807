package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.UserNotice;
import com.xiliulou.electricity.mapper.UserNoticeMapper;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.service.UserNoticeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/10/9 16:22
 * @Description:
 */

@Service
public class UserNoticeServiceImpl implements UserNoticeService {

	@Resource
	UserNoticeMapper userNoticeMapper;
	
	@Resource
	private RedisService redisService;

	@Slave
	@Override
	public R queryUserNotice() {
		//tenant
		Integer tenantId = TenantContextHolder.getTenantId();

		UserNotice userNotice = userNoticeMapper.selectLatest(tenantId);
		return R.ok(userNotice);
	}



	@Override
	public Triple<Boolean, String, Object> update(UserNoticeQuery userNoticeQuery, Long uid) {
		boolean result = redisService.setNx(CacheConstant.CACHE_USER_NOTICE_UPDATE_LOCK + uid, "1", 2 * 1000L, false);
		if (!result) {
			return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
		}
		
		if (Objects.isNull(userNoticeQuery.getId())) {
			UserNotice userNotice = new UserNotice();
			userNotice.setContent(userNoticeQuery.getContent());
			userNotice.setCreateTime(System.currentTimeMillis());
			userNotice.setUpdateTime(System.currentTimeMillis());
			userNotice.setTenantId(TenantContextHolder.getTenantId());
			userNoticeMapper.insert(userNotice);
		} else {
			
			UserNotice userNotice = new UserNotice();
			userNotice.setId(userNoticeQuery.getId());
			userNotice.setContent(userNoticeQuery.getContent());
			userNotice.setUpdateTime(System.currentTimeMillis());
			userNotice.setTenantId(TenantContextHolder.getTenantId());
			userNoticeMapper.update(userNotice);
		}
		
		return Triple.of(true, null, null);
	}
}
