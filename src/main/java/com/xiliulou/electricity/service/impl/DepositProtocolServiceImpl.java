package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.DepositProtocol;
import com.xiliulou.electricity.entity.UserNotice;
import com.xiliulou.electricity.mapper.DepositProtocolMapper;
import com.xiliulou.electricity.mapper.UserNoticeMapper;
import com.xiliulou.electricity.query.DepositProtocolQuery;
import com.xiliulou.electricity.query.UserNoticeQuery;
import com.xiliulou.electricity.service.DepositProtocolService;
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
public class DepositProtocolServiceImpl implements DepositProtocolService {

	@Resource
	DepositProtocolMapper depositProtocolMapper;

	@Slave
	@Override
	public R queryDepositProtocol() {
		//tenant
		Integer tenantId = TenantContextHolder.getTenantId();

		DepositProtocol depositProtocol = depositProtocolMapper.selectOne(new LambdaQueryWrapper<DepositProtocol>().eq(DepositProtocol::getTenantId, tenantId));
		return R.ok(depositProtocol);
	}



	@Override
	public Triple<Boolean, String, Object> update(DepositProtocolQuery depositProtocolQuery) {
		if (Objects.isNull(depositProtocolQuery.getId())) {
	
			DepositProtocol depositProtocol = new DepositProtocol();
			depositProtocol.setContent(depositProtocolQuery.getContent());
			depositProtocol.setCreateTime(System.currentTimeMillis());
			depositProtocol.setUpdateTime(System.currentTimeMillis());
			depositProtocol.setTenantId(TenantContextHolder.getTenantId());
			depositProtocolMapper.insert(depositProtocol);
		} else {

			DepositProtocol depositProtocol = new DepositProtocol();
			depositProtocol.setId(depositProtocolQuery.getId());
			depositProtocol.setContent(depositProtocolQuery.getContent());
			depositProtocol.setUpdateTime(System.currentTimeMillis());
			depositProtocol.setTenantId(TenantContextHolder.getTenantId());
			depositProtocolMapper.update(depositProtocol);
		}
		return Triple.of(true, null, null);
	}
}
