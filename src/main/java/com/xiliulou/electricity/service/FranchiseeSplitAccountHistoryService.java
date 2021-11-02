package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FranchiseeSplitAccountHistory;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;

/**
 * (FranchiseeSplitAccountHistory)表服务接口
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface FranchiseeSplitAccountHistoryService {

    /**
     * 新增数据
     *
     * @param franchiseeSplitAccountHistory 实例对象
     * @return 实例对象
     */
    FranchiseeSplitAccountHistory insert(FranchiseeSplitAccountHistory franchiseeSplitAccountHistory);

    R queryList(FranchiseeAccountQuery franchiseeAccountQuery);

    R queryCount(FranchiseeAccountQuery franchiseeAccountQuery);
}
