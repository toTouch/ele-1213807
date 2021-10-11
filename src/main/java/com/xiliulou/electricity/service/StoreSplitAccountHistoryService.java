package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.StoreSplitAccountHistory;
import com.xiliulou.electricity.query.StoreAccountQuery;

import java.util.List;

/**
 * (StoreSplitAccountHistory)表服务接口
 *
 * @author makejava
 * @since 2021-05-06 20:09:27
 */
public interface StoreSplitAccountHistoryService {
    /**
     * 新增数据
     *
     * @param storeSplitAccountHistory 实例对象
     * @return 实例对象
     */
    StoreSplitAccountHistory insert(StoreSplitAccountHistory storeSplitAccountHistory);


    R queryList(StoreAccountQuery storeAccountQuery);

    R queryCount(StoreAccountQuery storeAccountQuery);
}
