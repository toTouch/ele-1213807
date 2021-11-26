package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserAmountHistory;
import com.xiliulou.electricity.query.UserAmountHistoryQuery;

/**
 * (FranchiseeSplitAccountHistory)表服务接口
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface UserAmountHistoryService {

    /**
     * 新增数据
     *
     * @param userAmountHistory 实例对象
     * @return 实例对象
     */
    UserAmountHistory insert(UserAmountHistory userAmountHistory);

    R queryList(UserAmountHistoryQuery userAmountHistoryQuery);

    R queryCount(UserAmountHistoryQuery userAmountHistoryQuery);
}
