package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.OrderProtocol;
import com.xiliulou.electricity.mapper.OrderProtocolMapper;
import com.xiliulou.electricity.query.OrderProtocolQuery;
import com.xiliulou.electricity.service.OrderProtocolService;
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
public class OrderProtocolServiceImpl implements OrderProtocolService {

	@Resource
	OrderProtocolMapper orderProtocolMapper;

	@Override
	public R queryOrderProtocol() {
		//tenant
		Integer tenantId = TenantContextHolder.getTenantId();

		OrderProtocol orderProtocol = orderProtocolMapper.selectOne(new LambdaQueryWrapper<OrderProtocol>().eq(OrderProtocol::getTenantId, tenantId));
		return R.ok(orderProtocol);
	}



	@Override
	public Triple<Boolean, String, Object> update(OrderProtocolQuery orderProtocolQuery) {
		if (Objects.isNull(orderProtocolQuery.getId())) {
		
			OrderProtocol orderProtocol = new OrderProtocol();
			orderProtocol.setContent(orderProtocolQuery.getContent());
			orderProtocol.setCreateTime(System.currentTimeMillis());
			orderProtocol.setUpdateTime(System.currentTimeMillis());
			orderProtocol.setTenantId(TenantContextHolder.getTenantId());
			orderProtocolMapper.insert(orderProtocol);
		} else {

			OrderProtocol orderProtocol = new OrderProtocol();
			orderProtocol.setId(orderProtocolQuery.getId());
			orderProtocol.setContent(orderProtocolQuery.getContent());
			orderProtocol.setUpdateTime(System.currentTimeMillis());
			orderProtocol.setTenantId(TenantContextHolder.getTenantId());
			orderProtocolMapper.update(orderProtocol);
		}
		return Triple.of(true, null, null);
	}
}
