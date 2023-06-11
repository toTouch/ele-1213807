package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.EleRefundOrderHistory;
import com.xiliulou.electricity.mapper.EleRefundOrderHistoryMapper;
import com.xiliulou.electricity.query.EleRefundHistoryQuery;
import com.xiliulou.electricity.service.EleRefundOrderHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
@Service("eleRefundOrderHistoryService")
@Slf4j
public class EleRefundOrderHistoryServiceImpl implements EleRefundOrderHistoryService {
	@Resource
	EleRefundOrderHistoryMapper eleRefundOrderHistoryMapper;


	/**
	 * 新增数据
	 *
	 * @param eleRefundOrderHistory 实例对象
	 * @return 实例对象
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public EleRefundOrderHistory insert(EleRefundOrderHistory eleRefundOrderHistory) {
		this.eleRefundOrderHistoryMapper.insert(eleRefundOrderHistory);
		return eleRefundOrderHistory;
	}


	@Slave
	@Override
	public R queryList(EleRefundHistoryQuery eleRefundHistoryQuery) {
		return R.ok(eleRefundOrderHistoryMapper.queryList(eleRefundHistoryQuery));
	}

	@Slave
	@Override
	public R queryCount(EleRefundHistoryQuery eleRefundHistoryQuery) {
		return R.ok(eleRefundOrderHistoryMapper.queryCount(eleRefundHistoryQuery));
	}

}
