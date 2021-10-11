package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeSplitAccountHistory;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;

/**
 * (FranchiseeSplitAccountHistory)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:27
 */
public interface FranchiseeSplitAccountHistoryMapper extends BaseMapper<FranchiseeSplitAccountHistory> {

	Object queryList(FranchiseeAccountQuery franchiseeAccountQuery);

	Object queryCount(FranchiseeAccountQuery franchiseeAccountQuery);
}
