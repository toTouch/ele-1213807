package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.UserAmountHistory;
import com.xiliulou.electricity.query.UserAmountHistoryQuery;

import java.util.List;

/**
 * (FranchiseeSplitAccountHistory)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:27
 */
public interface UserAmountHistoryMapper extends BaseMapper<UserAmountHistory> {

	List<UserAmountHistory> queryList(UserAmountHistoryQuery userAmountHistoryQuery);

	Integer queryCount(UserAmountHistoryQuery userAmountHistoryQuery);
}
