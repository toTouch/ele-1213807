package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreShops;
import com.xiliulou.electricity.mapper.StoreShopsMapper;
import com.xiliulou.electricity.query.StoreShopsQuery;
import com.xiliulou.electricity.service.StoreShopsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/9/18 17:31
 * @Description:
 */
@Service("storeShopsService")
public class StoreShopsServiceImpl implements StoreShopsService {

	@Resource
	StoreShopsMapper storeShopsMapper;

	@Override
	public R insert(StoreShops storeShops) {
		return R.ok(storeShopsMapper.insert(storeShops));
	}

	@Override
	public R update(StoreShops storeShops) {
		return R.ok(storeShopsMapper.updateById(storeShops));
	}

	@Override
	public R delete(Long id) {
		StoreShops storeShops=storeShopsMapper.selectById(id);
		if(Objects.isNull(storeShops)){
			return R.fail();
		}

		storeShops.setId(id);
		storeShops.setUpdateTime(System.currentTimeMillis());
		storeShops.setDelFlag(StoreShops.DEL_DEL);
		storeShopsMapper.updateById(storeShops);
		return R.ok();
	}

	@Override
	public R queryList(StoreShopsQuery storeShopsQuery) {
		return R.ok(storeShopsMapper.queryList(storeShopsQuery));
	}

	@Override
	public R queryCount(StoreShopsQuery storeShopsQuery) {
		return R.ok(storeShopsMapper.queryCount(storeShopsQuery));
	}
}
