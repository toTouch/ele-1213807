package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.query.MutualExchangePageQuery;

import java.util.List;


public interface TenantFranchiseeMutualExchangeMapper extends BaseMapper<TenantFranchiseeMutualExchange> {
    
    List<TenantFranchiseeMutualExchange> selectMutualExchangeConfigListFromDB(Integer tenantId);
    
    TenantFranchiseeMutualExchange selectOneById(Long id);
    
    List<TenantFranchiseeMutualExchange> selectPageList(MutualExchangePageQuery query);
    
    Long countTotal(MutualExchangePageQuery query);
    
    Integer updateMutualExchangeById(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange);
    
}
