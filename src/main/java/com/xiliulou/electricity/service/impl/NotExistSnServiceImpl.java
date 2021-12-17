package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.mapper.NotExistSnServiceMapper;
import com.xiliulou.electricity.query.NotExistSnQuery;
import com.xiliulou.electricity.service.NotExistSnService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: Miss.Li
 * @Date: 2021/12/17 13:46
 * @Description:
 */
public class NotExistSnServiceImpl implements NotExistSnService {

	@Autowired
	NotExistSnServiceMapper notExistSnServiceMapper;


	@Override
	public void insert(NotExistSn notExistSn) {
		notExistSnServiceMapper.insert(notExistSn);
	}

	@Override
	public void update(NotExistSn notExistSn) {
		notExistSnServiceMapper.updateById(notExistSn);
	}

	@Override
	public NotExistSn queryByBatteryName(String batteryName) {
		return notExistSnServiceMapper.selectOne(new LambdaQueryWrapper<NotExistSn>()
		.eq(NotExistSn::getBatteryName,batteryName).eq(NotExistSn::getDelFlag,NotExistSn.DEL_NORMAL));
	}

	@Override
	public R queryList(NotExistSnQuery notExistSnQuery) {
		return R.ok(notExistSnServiceMapper.queryList(notExistSnQuery));
	}

	@Override
	public R queryCount(NotExistSnQuery notExistSnQuery) {
		return R.ok(notExistSnServiceMapper.queryCount(notExistSnQuery));
	}
}
