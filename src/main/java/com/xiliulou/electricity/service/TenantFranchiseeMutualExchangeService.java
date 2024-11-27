package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;

import java.util.List;

public interface TenantFranchiseeMutualExchangeService {
    
    R addConfig(MutualExchangeAddConfigRequest request);
    
    
    List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromDB(Integer tenantId);
    
    
    List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromCache(Integer tenantId);
    
    
    void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange);
    
}
