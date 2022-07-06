package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreGoods;
import com.xiliulou.electricity.query.StoreShopsQuery;

/**
 * @author: Miss.Li
 * @Date: 2021/9/18 17:31
 * @Description:
 */
public interface StoreGoodsService {

	R insert(StoreGoods storeGoods);

	R update(StoreGoods storeGoods);

	R delete(Long id);

	R queryList(StoreShopsQuery storeShopsQuery);

	R queryCount(StoreShopsQuery storeShopsQuery);

	StoreGoods queryByStoreIdAndCarModelId(Long storeId,Integer carModelId);

}
