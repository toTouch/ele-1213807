package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreShops;
import com.xiliulou.electricity.query.StoreAddAndUpdate;
import com.xiliulou.electricity.query.StoreQuery;
import com.xiliulou.electricity.query.StoreShopsQuery;

/**
 * @author: Miss.Li
 * @Date: 2021/9/18 17:31
 * @Description:
 */
public interface StoreShopsService {

	R insert(StoreShops storeShops);

	R update(StoreShops storeShops);

	R delete(Long id);

	R queryList(StoreShopsQuery storeShopsQuery);

	R queryCount(StoreShopsQuery storeShopsQuery);


}
