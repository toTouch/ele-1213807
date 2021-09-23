package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreGoods;
import com.xiliulou.electricity.mapper.StoreGoodsMapper;
import com.xiliulou.electricity.query.StoreShopsQuery;
import com.xiliulou.electricity.service.StoreGoodsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author: Miss.Li
 * @Date: 2021/9/18 17:31
 * @Description:
 */
@Service("storeGoodsService")
public class StoreGoodsServiceImpl implements StoreGoodsService {

	@Resource
	StoreGoodsMapper storeGoodsMapper;

	@Override
	public R insert(StoreGoods storeGoods) {
		return R.ok(storeGoodsMapper.insert(storeGoods));
	}

	@Override
	public R update(StoreGoods storeGoods) {
		return R.ok(storeGoodsMapper.updateById(storeGoods));
	}

	@Override
	public R delete(Long id) {
		StoreGoods storeGoods = storeGoodsMapper.selectById(id);
		if(Objects.isNull(storeGoods)){
			R.fail("ELECTRICITY.00109", "未找到门店商品");
		}

		storeGoods.setId(id);
		storeGoods.setUpdateTime(System.currentTimeMillis());
		storeGoods.setDelFlag(StoreGoods.DEL_DEL);
		storeGoodsMapper.updateById(storeGoods);
		return R.ok();
	}

	@Override
	public R queryList(StoreShopsQuery storeShopsQuery) {
		return R.ok(storeGoodsMapper.queryList(storeShopsQuery));
	}

	@Override
	public R queryCount(StoreShopsQuery storeShopsQuery) {
		return R.ok(storeGoodsMapper.queryCount(storeShopsQuery));
	}
}
