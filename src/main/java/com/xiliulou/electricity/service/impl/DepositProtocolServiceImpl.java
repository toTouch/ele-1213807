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
import com.xiliulou.electricity.vo.DepositProtocolVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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

		//3.0 中为了fix 查询时存在多条记录的bug
		DepositProtocol query = new DepositProtocol();
		query.setTenantId(tenantId);

		List<DepositProtocol> protocolList = depositProtocolMapper.selectByQuery(query);
		DepositProtocolVO depositProtocolVO = new DepositProtocolVO();
		if(CollectionUtils.isNotEmpty(protocolList) && protocolList.size() > 0){
			DepositProtocol depositProtocol = protocolList.get(0);
			BeanUtils.copyProperties(depositProtocol, depositProtocolVO);
			return R.ok(depositProtocolVO);
		}

		//DepositProtocol depositProtocol = depositProtocolMapper.selectOne(new LambdaQueryWrapper<DepositProtocol>().eq(DepositProtocol::getTenantId, tenantId));
		return R.ok(depositProtocolVO);
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
