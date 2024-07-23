package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.converter.AlipayAppConfigConverter;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.mapper.AlipayAppConfigMapper;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.vo.AlipayAppConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 支付宝小程序配置(AlipayAppConfig)表服务实现类
 *
 * @author zzlong
 * @since 2024-07-08 16:45:19
 */
@Service("alipayAppConfigService")
@Slf4j
public class AlipayAppConfigServiceImpl implements AlipayAppConfigService {
    
    @Resource
    private AlipayAppConfigMapper alipayAppConfigMapper;
    
    @Resource
    private RedisService redisService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Slave
    @Override
    public AlipayAppConfig queryByAppId(String appId) {
        return this.alipayAppConfigMapper.selectByAppId(appId);
    }
    
    // TODO 移除
    @Slave
    @Override
    public AlipayAppConfig queryByTenantId(Integer tenantId) {
        return this.alipayAppConfigMapper.selectByTenantId(tenantId);
    }
    
    @Slave
    @Override
    public List<AlipayAppConfigVO> listByTenantId(Integer tenantId) {
        List<AlipayAppConfig> list = this.alipayAppConfigMapper.selectListByTenantId(tenantId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
    
        return list.stream().map(item -> {
            AlipayAppConfigVO alipayAppConfigVO = new AlipayAppConfigVO();
            BeanUtils.copyProperties(item, alipayAppConfigVO);
        
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            alipayAppConfigVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
        
            return alipayAppConfigVO;
        }).collect(Collectors.toList());
    }
    
    @Override
    public AlipayAppConfigBizDetails queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws AliPayException {
        try {
            // 批量查询缓存
            List<AlipayAppConfig> electricityPayParamsList = this.queryFromCacheList(tenantId, Sets.newHashSet(franchiseeId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE));
            if (CollectionUtils.isEmpty(electricityPayParamsList)) {
                log.warn("WARN! WeChat Pay parameter is not configured,tenantId={},franchiseeId={}", tenantId, franchiseeId);
                return null;
            }
            
            Map<Long, AlipayAppConfig> payParamsMap = electricityPayParamsList.stream().collect(Collectors.toMap(AlipayAppConfig::getFranchiseeId, v -> v, (k1, k2) -> k1));
            
            // 先取加盟商配置
            AlipayAppConfig payParams = payParamsMap.get(franchiseeId);
            
            if (Objects.isNull(payParams)) {
                // 加盟商未配置 取默认配置
                payParams = payParamsMap.get(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
            }
            AlipayAppConfigBizDetails alipayAppConfigBizDetails = AlipayAppConfigConverter.qryDoToDetails(payParams);
            
            return alipayAppConfigBizDetails;
        } catch (Exception e) {
            log.warn("AlipayAppConfigServiceImpl.queryByTenantIdAndFranchiseeId WARN! :", e);
            throw new AliPayException("支付配置获取失败");
        }
        
    }
    
    @Override
    public AlipayAppConfigBizDetails queryPreciseByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws AliPayException {
        try {
            List<AlipayAppConfig> electricityPayParamsList = this.queryFromCacheList(tenantId, Sets.newHashSet(franchiseeId));
            if (CollectionUtils.isEmpty(electricityPayParamsList)) {
                return null;
            }
            AlipayAppConfigBizDetails alipayAppConfigBizDetails = AlipayAppConfigConverter.qryDoToDetails(electricityPayParamsList.get(0));
            return alipayAppConfigBizDetails;
        } catch (Exception e) {
            log.warn("AlipayAppConfigServiceImpl.queryByTenantIdAndFranchiseeId WARN! :", e);
            throw new AliPayException("支付配置获取失败");
        }
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AlipayAppConfig queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param alipayAppConfig 实例对象
     * @return 实例对象
     */
    @Override
    public Integer update(AlipayAppConfig alipayAppConfig) {
        int update = this.alipayAppConfigMapper.update(alipayAppConfig);
        redisService.delete(buildCacheKey(alipayAppConfig.getTenantId(), alipayAppConfig.getFranchiseeId()));
        return update;
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Integer deleteById(Long id) {
        return this.alipayAppConfigMapper.deleteById(id);
    }
    
    
    /**
     * 批量查询缓存
     *
     * @param tenantId
     * @param franchiseeIdSet
     * @author caobotao.cbt
     * @date 2024/6/12 13:38
     */
    private List<AlipayAppConfig> queryFromCacheList(Integer tenantId, Set<Long> franchiseeIdSet) {
        
        // 从缓存中获取数据
        List<String> cacheKeys = franchiseeIdSet.stream().map(franchiseeId -> buildCacheKey(tenantId, franchiseeId)).collect(Collectors.toList());
        List<AlipayAppConfig> cacheList = redisService.multiJsonGet(cacheKeys, AlipayAppConfig.class);
        
        // key：franchiseeId
        Map<Long, AlipayAppConfig> cacheFranchiseeIdMap = Optional.ofNullable(cacheList).orElse(Collections.emptyList()).stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(AlipayAppConfig::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
        
        // 查询数据库的集合
        List<Long> qryDbList = new ArrayList<>(franchiseeIdSet.size());
        
        List<AlipayAppConfig> payParams = new ArrayList<>(franchiseeIdSet.size());
        
        franchiseeIdSet.forEach(fid -> {
            AlipayAppConfig cache = cacheFranchiseeIdMap.get(fid);
            if (Objects.isNull(cache)) {
                // 缓存不存在，查询数据库
                qryDbList.add(fid);
            } else if (Objects.nonNull(cache.getId())) {
                // 缓存存在 并且有id 则说明当前加盟商已配置
                payParams.add(cache);
            }
        });
        
        if (CollectionUtils.isEmpty(qryDbList)) {
            return payParams;
        }
        
        // 从数据库查询数据
        List<AlipayAppConfig> dbList = alipayAppConfigMapper.selectListByTenantIdAndFranchiseeIds(tenantId, qryDbList);
        
        if (CollectionUtils.isNotEmpty(dbList)) {
            // 添加到结果集
            payParams.addAll(dbList);
        }
        
        // 更新缓存
        Map<String, String> cacheSaveMap = Optional.ofNullable(dbList).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(e -> buildCacheKey(e.getTenantId(), e.getFranchiseeId()), v -> JsonUtil.toJson(v), (k1, k2) -> k1));
        
        // 处理不存在的数据
        qryDbList.stream().filter(franchiseeId -> !cacheSaveMap.containsKey(buildCacheKey(tenantId, franchiseeId))).forEach(franchiseeId -> {
            AlipayAppConfig nullParam = new AlipayAppConfig();
            nullParam.setTenantId(tenantId);
            nullParam.setFranchiseeId(franchiseeId);
            cacheSaveMap.put(buildCacheKey(tenantId, franchiseeId), JsonUtil.toJson(nullParam));
        });
        
        // 批量设置缓存
        redisService.multiSet(cacheSaveMap);
        
        return payParams;
    }
    
    
    /**
     * 构建缓存key
     */
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(CacheConstant.ELE_ALI_PAY_PARAMS_KEY, tenantId, franchiseeId);
    }
}
