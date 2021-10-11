package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.StoreAmount;
import com.xiliulou.electricity.query.StoreAccountQuery;

/**
 * (StoreAmount)表服务接口
 *
 * @author makejava
 * @since 2021-05-06 20:09:26
 */
public interface StoreAmountService {

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreAmount queryByStoreIdFromCache(Long id);


    /**
     * 新增数据
     *
     * @param storeAmount 实例对象
     * @return 实例对象
     */
    StoreAmount insert(StoreAmount storeAmount);

    /**
     * 修改数据
     *
     * @param storeAmount 实例对象
     * @return 实例对象
     */
    Integer update(StoreAmount storeAmount);


	void handleSplitAccount(Store store, ElectricityTradeOrder payAmount, int percent);

	R queryList(StoreAccountQuery storeAccountQuery);

	R queryCount(StoreAccountQuery storeAccountQuery);
}
