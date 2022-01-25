package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.mapper.NotExistSnServiceMapper;
import com.xiliulou.electricity.query.NotExistSnQuery;
import com.xiliulou.electricity.service.NotExistSnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: Miss.Li
 * @Date: 2021/12/17 13:46
 * @Description:
 */
@Service
public class NotExistSnServiceImpl implements NotExistSnService {

	@Resource
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
	public NotExistSn queryByBatteryName(String batteryName, Integer electricityCabinetId, Integer cellNo) {
		return notExistSnServiceMapper.selectOne(new LambdaQueryWrapper<NotExistSn>()
				.eq(NotExistSn::getBatteryName, batteryName).eq(NotExistSn::getDelFlag, NotExistSn.DEL_NORMAL)
				.eq(NotExistSn::getEId, electricityCabinetId).eq(NotExistSn::getCellNo, cellNo));
	}

	@Override
	public R queryList(NotExistSnQuery notExistSnQuery) {
		return R.ok(notExistSnServiceMapper.queryList(notExistSnQuery));
	}

	@Override
	public R queryCount(NotExistSnQuery notExistSnQuery) {
		return R.ok(notExistSnServiceMapper.queryCount(notExistSnQuery));
	}

	@Override
	public NotExistSn queryByIdFromDB(Long id) {
		return notExistSnServiceMapper.selectById(id);
	}

	@Override
	public void delete(Long id) {
		notExistSnServiceMapper.deleteById(id);
	}
}
