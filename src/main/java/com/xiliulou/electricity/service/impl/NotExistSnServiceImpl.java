package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.mapper.NotExistSnMapper;
import com.xiliulou.electricity.query.NotExistSnQuery;
import com.xiliulou.electricity.service.NotExistSnService;
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
	NotExistSnMapper notExistSnMapper;

	@Override
	public void insert(NotExistSn notExistSn) {
		notExistSnMapper.insert(notExistSn);
	}

	@Override
	public void update(NotExistSn notExistSn) {
		notExistSnMapper.updateById(notExistSn);
	}

	@Override
	public NotExistSn queryByOther(String batteryName, Integer electricityCabinetId, Integer cellNo) {
		return notExistSnMapper.selectOne(new LambdaQueryWrapper<NotExistSn>()
				.eq(NotExistSn::getBatteryName, batteryName).eq(NotExistSn::getDelFlag, NotExistSn.DEL_NORMAL)
				.eq(NotExistSn::getEId, electricityCabinetId).eq(NotExistSn::getCellNo, cellNo));
	}

	@Override
	public NotExistSn queryByBatteryName(String batteryName) {
		return notExistSnMapper.selectOne(new LambdaQueryWrapper<NotExistSn>()
				.eq(NotExistSn::getBatteryName, batteryName).eq(NotExistSn::getDelFlag, NotExistSn.DEL_NORMAL));
	}

	@Override
	public R queryList(NotExistSnQuery notExistSnQuery) {
		return R.ok(notExistSnMapper.queryList(notExistSnQuery));
	}

	@Override
	public R queryCount(NotExistSnQuery notExistSnQuery) {
		return R.ok(notExistSnMapper.queryCount(notExistSnQuery));
	}

	@Override
	public NotExistSn queryByIdFromDB(Long id) {
		return notExistSnMapper.selectById(id);
	}

	@Override
	public void delete(Long id) {
		notExistSnMapper.deleteById(id);
	}
	
	@Override
	public int deleteNotExistSn(NotExistSn entity) {
		NotExistSn notExistSn = new NotExistSn();
		notExistSn.setId(entity.getId());
		notExistSn.setDelFlag(NotExistSn.DEL_DEL);
		notExistSn.setUpdateTime(System.currentTimeMillis());
		
		return notExistSnMapper.updateById(notExistSn);
	}
}
