package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.query.MutualExchangePageQuery;
import com.xiliulou.electricity.query.MutualExchangeUpdateQuery;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.vo.MutualExchangeDetailVO;

import java.util.List;

public interface TenantFranchiseeMutualExchangeService {
    
    R addOrEditConfig(MutualExchangeAddConfigRequest request);
    
    MutualExchangeDetailVO getMutualExchangeDetailById(Long id);
    
    
    void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange);
    
    void updateMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange);
    
    
    List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromDB(Integer tenantId);
    
    
    List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromCache(Integer tenantId);
    
    List<MutualExchangeDetailVO> pageList(MutualExchangePageQuery query);
    
    Long pageCount(MutualExchangePageQuery query);
    
    R deleteById(Long id);
    
    R updateStatus(MutualExchangeUpdateQuery query);
}
