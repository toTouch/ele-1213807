package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FranchiseeAmount;
import com.xiliulou.electricity.query.FranchiseeAccountQuery;

import java.util.List;

/**
 * (FranchiseeAmount)表数据库访问层
 *
 * @author makejava
 * @since 2021-05-06 20:09:28
 */
public interface FranchiseeAmountMapper extends BaseMapper<FranchiseeAmount> {


	List<FranchiseeAmount> queryList(FranchiseeAccountQuery franchiseeAccountQuery);

	Integer queryCount(FranchiseeAccountQuery franchiseeAccountQuery);

	void deleteByFranchiseeId(Long id);
}
