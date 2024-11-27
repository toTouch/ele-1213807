package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;

import java.util.List;


public interface TenantFranchiseeMutualExchangeMapper extends BaseMapper<TenantFranchiseeMutualExchange> {
    
    List<TenantFranchiseeMutualExchange> selectMutualExchangeConfigListFromDB(Integer tenantId);
}
