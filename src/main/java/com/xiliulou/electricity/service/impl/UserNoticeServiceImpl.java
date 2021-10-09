package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserNotice;
import com.xiliulou.electricity.mapper.UserNoticeMapper;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.service.UserNoticeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: Miss.Li
 * @Date: 2021/10/9 16:22
 * @Description:
 */

@Service
public class UserNoticeServiceImpl implements UserNoticeService {

	@Resource
	UserNoticeMapper  userNoticeMapper;

	@Override
	public Object queryUserNotice() {
		Integer tenantId = TenantContextHolder.getTenantId();

		UserNotice userNotice =userNoticeMapper.selectOne(new LambdaQueryWrapper<UserNotice>().eq(UserNotice::getTenantId,tenantId));
		return R.ok(userNotice);
	}

	@Override
	public Triple<Boolean, String, Object> insert(UserNoticeQuery userNoticeQuery) {
		Integer tenantId = TenantContextHolder.getTenantId();

		UserNotice userNotice = new UserNotice();
		userNotice.setContent(userNoticeQuery.getContent());
		userNotice.setCreateTime(System.currentTimeMillis());
		userNotice.setUpdateTime(System.currentTimeMillis());
		userNotice.setTenantId(tenantId);
		userNoticeMapper.insert(userNotice);

		return Triple.of(true, null, null);
	}

	@Override
	public Triple<Boolean, String, Object> update(UserNoticeQuery userNoticeQuery) {


		UserNotice userNotice = new UserNotice();
		userNotice.setId(userNoticeQuery.getId());
		userNotice.setContent(userNoticeQuery.getContent());
		userNotice.setUpdateTime(System.currentTimeMillis());
		userNoticeMapper.updateById(userNotice);
		return Triple.of(true, null, null);
	}
}
