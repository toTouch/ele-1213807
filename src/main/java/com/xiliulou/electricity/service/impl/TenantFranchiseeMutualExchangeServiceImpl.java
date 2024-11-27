package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.TenantFranchiseeMutualExchange;
import com.xiliulou.electricity.mapper.TenantFranchiseeMutualExchangeMapper;
import com.xiliulou.electricity.query.MutualExchangePageQuery;
import com.xiliulou.electricity.query.MutualExchangeUpdateQuery;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.MutualExchangeDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    
    
    public static final Integer MAX_MUTUAL_EXCHANGE_CONFIG_COUNT = 5;
    
    @Override
    public R addOrEditConfig(MutualExchangeAddConfigRequest request) {
        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        if (CollUtil.isEmpty(combinedFranchisee)) {
            return R.fail("302000", "互换配置不能为空");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        List<TenantFranchiseeMutualExchange> mutualExchangeList = getMutualExchangeConfigListFromCache(tenantId);
        if (isExistMutualExchangeConfig(combinedFranchisee, mutualExchangeList)) {
            return R.fail("302001", "该互换配置已存在");
        }
        
        TenantFranchiseeMutualExchange mutualExchange = TenantFranchiseeMutualExchange.builder().combinedName(request.getCombinedName())
                .combinedFranchisee(JsonUtil.toJson(combinedFranchisee)).status(request.getStatus()).updateTime(System.currentTimeMillis()).build();
        if (Objects.isNull(request.getId())) {
            if (Objects.equals(mutualExchangeList.size(), MAX_MUTUAL_EXCHANGE_CONFIG_COUNT)) {
                return R.fail("302002", "最多添加5个配置");
            }
            mutualExchange.setTenantId(tenantId).setCreateTime(System.currentTimeMillis());
            saveMutualExchange(mutualExchange);
        } else {
            mutualExchange.setId(request.getId());
            updateMutualExchange(mutualExchange);
            //  删除缓存
            redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId);
        }
        // todo 操作记录
        
        return R.ok();
    }
    
    @Override
    public MutualExchangeDetailVO getMutualExchangeDetailById(Long id) {
        return BeanUtil.copyProperties(mutualExchangeMapper.selectOneById(id), MutualExchangeDetailVO.class);
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
        List<TenantFranchiseeMutualExchange> mutualExchangesFromCache = redisService.getWithList(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId,
                TenantFranchiseeMutualExchange.class);
        if (CollUtil.isNotEmpty(mutualExchangesFromCache)) {
            return mutualExchangesFromCache;
        }
        
        List<TenantFranchiseeMutualExchange> mutualExchangeList = getMutualExchangeConfigListFromDB(tenantId);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }
        
        redisService.saveWithList(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId, mutualExchangeList);
        return mutualExchangeList;
    }
    
    @Override
    public void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.insert(tenantFranchiseeMutualExchange);
    }
    
    
    @Override
    public void updateMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.updateMutualExchangeById(tenantFranchiseeMutualExchange);
    }
    
    
    @Override
    public List<MutualExchangeDetailVO> pageList(MutualExchangePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        List<TenantFranchiseeMutualExchange> mutualExchangeList = mutualExchangeMapper.selectPageList(query);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }
        return BeanUtil.copyToList(mutualExchangeList, MutualExchangeDetailVO.class);
    }
    
    @Override
    public Long pageCount(MutualExchangePageQuery query) {
        query.setTenantId(TenantContextHolder.getTenantId());
        return mutualExchangeMapper.countTotal(query);
    }
    
    
    @Override
    public R deleteById(Long id) {
        TenantFranchiseeMutualExchange mutualExchange = mutualExchangeMapper.selectOneById(id);
        if (Objects.isNull(mutualExchange)) {
            return R.fail("302003", "不存在的互换配置");
        }
        this.updateMutualExchange(TenantFranchiseeMutualExchange.builder().id(id).updateTime(System.currentTimeMillis()).delFlag(1).build());
        redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + TenantContextHolder.getTenantId());
        return R.ok();
    }
    
    
    @Override
    public R updateStatus(MutualExchangeUpdateQuery query) {
        TenantFranchiseeMutualExchange mutualExchange = mutualExchangeMapper.selectOneById(query.getId());
        if (Objects.isNull(mutualExchange)) {
            return R.fail("302003", "不存在的互换配置");
        }
        this.updateMutualExchange(TenantFranchiseeMutualExchange.builder().id(query.getId()).updateTime(System.currentTimeMillis()).status(query.getStatus()).build());
        redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + TenantContextHolder.getTenantId());
        return R.ok();
    }
    
    
    @Override
    public Pair<Boolean, Set<Long>> isSatisfyFranchiseeIdMutualExchange(Integer tenantId, Long franchiseeId) {
        if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId)) {
            log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! tenantId or franchiseeId is null");
            return Pair.of(false, null);
        }
        List<TenantFranchiseeMutualExchange> mutualExchangeList = getMutualExchangeConfigListFromCache(tenantId);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! Current Tenant MutualExchangeConfig is null, tenantId is {}", tenantId);
            return Pair.of(false, null);
        }
        
        Set<Long> franchiseeIdSet = new HashSet<>();
        mutualExchangeList.forEach(e -> {
            List<Long> combinedFranchisee = JsonUtil.fromJsonArray(e.getCombinedFranchisee(), Long.class);
            if (combinedFranchisee.contains(franchiseeId)) {
                franchiseeIdSet.addAll(combinedFranchisee);
            }
        });
        // 有互通，但是当前加盟商并没有在互通中
        if (CollUtil.isEmpty(franchiseeIdSet)) {
            return Pair.of(false, null);
        }
        
        log.info("IsSatisfyFranchiseeIdMutualExchange Info! franchiseeIdSet is {}", JsonUtil.toJson(franchiseeIdSet));
        return Pair.of(true, franchiseeIdSet);
    }
    
    /**
     * 是否存在互换配置
     *
     * @param franchiseeList franchiseeList
     * @param list           list
     * @return Boolean
     */
    private Boolean isExistMutualExchangeConfig(List<Long> franchiseeList, List<TenantFranchiseeMutualExchange> list) {
        Set<Long> franchiseeSet = new HashSet<>(franchiseeList);
        for (TenantFranchiseeMutualExchange mutualExchange : list) {
            Set<Long> combinedFranchisee = new HashSet<>(JsonUtil.fromJsonArray(mutualExchange.getCombinedFranchisee(), Long.class));
            if (combinedFranchisee.containsAll(franchiseeSet)) {
                return true;
            }
        }
        
        return false;
    }
}
