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
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
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
    
    
    public static final Integer MAX_MUTUAL_EXCHANGE_CONFIG_COUNT = 5;
    
    @Override
    public R addOrEditConfig(MutualExchangeAddConfigRequest request) {
        List<Long> combinedFranchisee = request.getCombinedFranchisee();
        if (CollUtil.isEmpty(combinedFranchisee)) {
            return R.fail("302000", "互换配置不能为空");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        List<String> mutualExchangeList = getMutualFranchiseeExchangeCache(tenantId);
        if (isExistMutualExchangeConfig(combinedFranchisee, mutualExchangeList)) {
            return R.fail("302001", "该互换配置已存在");
        }
        
        TenantFranchiseeMutualExchange mutualExchange = TenantFranchiseeMutualExchange.builder().combinedName(request.getCombinedName()).tenantId(tenantId)
                .combinedFranchisee(JsonUtil.toJson(combinedFranchisee)).status(request.getStatus()).createTime(System.currentTimeMillis()).updateTime(System.currentTimeMillis())
                .build();
        if (Objects.isNull(request.getId())) {
            if (Objects.equals(mutualExchangeList.size(), MAX_MUTUAL_EXCHANGE_CONFIG_COUNT)) {
                return R.fail("302002", "最多添加5个配置");
            }
            saveMutualExchange(mutualExchange);
        } else {
            mutualExchange.setId(request.getId());
            updateMutualExchange(mutualExchange);
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
    
    
    /**
     * 为避免redis的大key，这里只是将组合加盟商字段放在缓存中
     *
     * @param tenantId tenantId tenantId
     * @return List<String>
     */
    @Override
    public List<String> getMutualFranchiseeExchangeCache(Integer tenantId) {
        if (Objects.isNull(tenantId)) {
            return CollUtil.newArrayList();
        }
        List<String> mutualExchangesFromCache = redisService.getWithList(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId, String.class);
        if (CollUtil.isNotEmpty(mutualExchangesFromCache)) {
            return mutualExchangesFromCache;
        }
        
        List<TenantFranchiseeMutualExchange> mutualExchangeList = getMutualExchangeConfigListFromDB(tenantId);
        if (CollUtil.isEmpty(mutualExchangeList)) {
            return CollUtil.newArrayList();
        }
        
        List<String> combinedFranchiseeList = mutualExchangeList.stream().map(TenantFranchiseeMutualExchange::getCombinedFranchisee).collect(Collectors.toList());
        
        redisService.saveWithList(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantId, combinedFranchiseeList);
        return combinedFranchiseeList;
    }
    
    @Override
    public void saveMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.insert(tenantFranchiseeMutualExchange);
        redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantFranchiseeMutualExchange.getTenantId());
    }
    
    
    @Override
    public void updateMutualExchange(TenantFranchiseeMutualExchange tenantFranchiseeMutualExchange) {
        mutualExchangeMapper.updateMutualExchangeById(tenantFranchiseeMutualExchange);
        redisService.delete(CacheConstant.MUTUAL_EXCHANGE_CONFIG_KEY + tenantFranchiseeMutualExchange.getTenantId());
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
        this.updateMutualExchange(
                TenantFranchiseeMutualExchange.builder().id(id).tenantId(TenantContextHolder.getTenantId()).updateTime(System.currentTimeMillis()).delFlag(1).build());
        return R.ok();
    }
    
    
    @Override
    public R updateStatus(MutualExchangeUpdateQuery query) {
        TenantFranchiseeMutualExchange mutualExchange = mutualExchangeMapper.selectOneById(query.getId());
        if (Objects.isNull(mutualExchange)) {
            return R.fail("302003", "不存在的互换配置");
        }
        this.updateMutualExchange(TenantFranchiseeMutualExchange.builder().id(query.getId()).tenantId(TenantContextHolder.getTenantId()).updateTime(System.currentTimeMillis())
                .status(query.getStatus()).build());
        return R.ok();
    }
    
    
    @Override
    public Pair<Boolean, Set<Long>> satisfyMutualExchangeFranchisee(Integer tenantId, Long franchiseeId) {
        try {
            if (Objects.isNull(tenantId) || Objects.isNull(franchiseeId)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! tenantId or franchiseeId is null");
                return Pair.of(false, null);
            }
            List<String> mutualFranchiseeList = getMutualFranchiseeExchangeCache(tenantId);
            if (CollUtil.isEmpty(mutualFranchiseeList)) {
                log.warn("IsSatisfyFranchiseeIdMutualExchange Warn! Current Tenant mutualFranchiseeList is null, tenantId is {}", tenantId);
                return Pair.of(false, null);
            }
            
            Set<Long> mutualFranchiseeSet = new HashSet<>();
            mutualFranchiseeList.forEach(e -> {
                List<Long> combinedFranchisee = JsonUtil.fromJsonArray(e, Long.class);
                if (combinedFranchisee.contains(franchiseeId)) {
                    mutualFranchiseeSet.addAll(combinedFranchisee);
                }
            });
            // 有互通配置，但是当前加盟商并没有在互通中
            if (CollUtil.isEmpty(mutualFranchiseeSet)) {
                return Pair.of(false, null);
            }
            log.info("IsSatisfyFranchiseeIdMutualExchange Info! Current Franchisee SatisfyMutualExchange,franchiseeId is {}, MutualFranchiseeSet is {}", franchiseeId,
                    JsonUtil.toJson(mutualFranchiseeSet));
            return Pair.of(true, mutualFranchiseeSet);
            
        } catch (Exception e) {
            log.error("IsSatisfyFranchiseeIdMutualExchange Error!", e);
        }
        return Pair.of(false, null);
    }
    
    
    @Override
    public Boolean isSatisfyFranchiseeMutualExchange(Integer tenantId, Long franchiseeId, Long otherFranchiseeId) {
        Pair<Boolean, Set<Long>> pair = satisfyMutualExchangeFranchisee(tenantId, franchiseeId);
        if (pair.getLeft()) {
            // 判断加盟商互通是否包含另一加盟商
            return pair.getRight().contains(otherFranchiseeId);
        }
        if (Objects.isNull(otherFranchiseeId)) {
            return false;
        }
        // 不符合互通配置,需要判断两个加盟商是否相等
        return Objects.equals(franchiseeId, otherFranchiseeId);
    }
    
    
    @Override
    public Triple<Boolean, String, Object> orderExchangeMutualFranchiseeCheck(Integer tenantId, Long franchiseeId, Long otherFranchiseeId) {
        Pair<Boolean, Set<Long>> mutualExchangeFranchiseePair = satisfyMutualExchangeFranchisee(tenantId, franchiseeId);
        if (mutualExchangeFranchiseePair.getLeft()) {
            if (mutualExchangeFranchiseePair.getRight().contains(otherFranchiseeId)) {
                // 存在互通加盟商
                return Triple.of(true, null, mutualExchangeFranchiseePair.getRight());
            } else {
                log.warn("ORDER WARN! user fId  is not equal franchiseeId, uidF is {}, eidF is {}", franchiseeId, otherFranchiseeId);
                return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
            }
        } else {
            if (Objects.nonNull(otherFranchiseeId) && Objects.equals(franchiseeId, otherFranchiseeId)) {
                return Triple.of(true, null, CollUtil.newHashSet().add(franchiseeId));
            } else {
                log.warn("ORDER WARN! user fId  is not equal franchiseeId, uidF is {}, eidF is {}", franchiseeId, otherFranchiseeId);
                return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
            }
        }
    }
    
    
    /**
     * 是否存在互换配置
     *
     * @param franchiseeList franchiseeList
     * @param list           list
     * @return Boolean
     */
    private Boolean isExistMutualExchangeConfig(List<Long> franchiseeList, List<String> list) {
        Set<Long> franchiseeSet = new HashSet<>(franchiseeList);
        for (String franchisee : list) {
            Set<Long> combinedFranchisee = new HashSet<>(JsonUtil.fromJsonArray(franchisee, Long.class));
            if (combinedFranchisee.containsAll(franchiseeSet)) {
                return true;
            }
        }
        return false;
    }
}
