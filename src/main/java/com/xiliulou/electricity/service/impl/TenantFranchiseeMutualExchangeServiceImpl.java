package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.mapper.TenantFranchiseeMutualExchangeMapper;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName: TenantFranchiseeMutualExchangeServiceImpl
 * @description:
 * @author: renhang
 * @create: 2024-11-27 15:25
 */
@Service
@Slf4j
public class TenantFranchiseeMutualExchangeServiceImpl implements TenantFranchiseeMutualExchangeService {
    
    @Resource
    private TenantFranchiseeMutualExchangeMapper mutualExchangeMapper;
    
    @Resource
    RedisService redisService;
    
    
    @Override
    public R addConfig(MutualExchangeAddConfigRequest request) {
        
        isExistMutualExchangeConfig();
        TenantFranchiseeMutualExchange.builder().combinedName(request.getCombinedName()).combinedFranchisee().build();
        return null;
    }
    
    @Override
    public List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromDB(Integer tenantId) {
        return mutualExchangeMapper.selectMutualExchangeConfigListFromDB(tenantId);
    }
    
    @Override
    public List<TenantFranchiseeMutualExchange> getMutualExchangeConfigListFromCache(Integer tenantId) {
        if (Objects.isNull(tenantId)) {
            return CollUtil.newArrayList();
        }
        List<TenantFranchiseeMutualExchange> mutualExchangesFromCache = redisService.getWithList(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY, TenantFranchiseeMutualExchange.class);
        if (CollUtil.isNotEmpty(mutualExchangesFromCache)) {
            return mutualExchangesFromCache;
        }
        
        List<TenantFranchiseeMutualExchange> mutualExchangeList = getMutualExchangeConfigListFromDB(tenantId);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }
        
        redisService.saveWithList(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY, mutualExchangeList);
        return mutualExchangeList;
    }
    
    @Override
    public void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.insert(tenantFranchiseeMutualExchange);
    }
    
    
    private Boolean isExistMutualExchangeConfig(Integer tenantId, List<String> franchiseeList) {
        List<TenantFranchiseeMutualExchange> list = getMutualExchangeConfigListFromCache(tenantId);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        
        Set<String> franchiseeSet = franchiseeList.stream().collect(Collectors.toSet());
        
        for (TenantFranchiseeMutualExchange mutualExchange : list) {
            Set<String> combinedFranchisee = JsonUtil.fromJsonArray(mutualExchange.getCombinedFranchisee(), String.class).stream().collect(Collectors.toSet());
            if (combinedFranchisee.containsAll(franchiseeSet)) {
                return true;
            }
        }
        
        return false;
    }
}
