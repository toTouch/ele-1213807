package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.pay.AlipayAppConfigBizDetails;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.converter.AlipayAppConfigConverter;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.enums.AliPayConfigTypeEnum;
import com.xiliulou.electricity.mapper.AlipayAppConfigMapper;
import com.xiliulou.electricity.query.AlipayAppConfigQuery;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.AlipayAppConfigVO;
import com.xiliulou.pay.alipay.exception.AliPayException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    
    @Override
    public Triple<Boolean, String, Object> save(AlipayAppConfigQuery query) {
        query.setFranchiseeId(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId, String.valueOf(System.currentTimeMillis()), 5 * 1000L, true)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Franchisee franchisee = null;
        if (AliPayConfigTypeEnum.FRANCHISEE_CONFIG.getType().equals(query.getConfigType())) {
            franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                return Triple.of(false, "100106", "加盟商不存在");
            }
        }
        
        Triple<Boolean, String, Object> verifyResult = verifySaveParams(query, tenantId);
        if (Boolean.FALSE.equals(verifyResult.getLeft())) {
            return verifyResult;
        }
        
        AlipayAppConfig alipayAppConfig = new AlipayAppConfig();
        BeanUtils.copyProperties(query, alipayAppConfig);
        alipayAppConfig.setDelFlag(CommonConstant.DEL_N);
        alipayAppConfig.setTenantId(tenantId);
        alipayAppConfig.setCreateTime(System.currentTimeMillis());
        alipayAppConfig.setUpdateTime(System.currentTimeMillis());
        alipayAppConfigMapper.insert(alipayAppConfig);
        
        redisService.delete(buildCacheKey(tenantId, query.getFranchiseeId()));
        
        // 操作记录
        this.operateRecord(Objects.isNull(franchisee) ? "" : franchisee.getName());
        
        return Triple.of(true, "", "");
    }
    
    private Triple<Boolean, String, Object> verifySaveParams(AlipayAppConfigQuery query, Integer tenantId) {
        AlipayAppConfig alipayAppConfig = alipayAppConfigMapper.selectBySellerIdAndTenantId(query.getSellerId(), tenantId);
        if (Objects.nonNull(alipayAppConfig)) {
            return Triple.of(false, "100440", "卖家支付宝账号与现有支付配置重复，请修改后操作");
        }
        
        List<AlipayAppConfig> existAlipayAppConfigs = queryFromCacheList(tenantId, Sets.newHashSet(query.getFranchiseeId(), MultiFranchiseeConstant.DEFAULT_FRANCHISEE));
        
        //运营商配置
        if (AliPayConfigTypeEnum.DEFAULT_CONFIG.getType().equals(query.getConfigType()) && CollectionUtils.isNotEmpty(existAlipayAppConfigs)) {
            return Triple.of(false, "100441", "默认配置已存在,请勿重复添加");
        }
        
        Map<Long, AlipayAppConfig> existMap = Optional.ofNullable(existAlipayAppConfigs).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(AlipayAppConfig::getFranchiseeId, v -> v, (k1, k2) -> k1));
        
        //加盟商配置
        if (AliPayConfigTypeEnum.FRANCHISEE_CONFIG.getType().equals(query.getConfigType())) {
            AlipayAppConfig defaultPayParams = existMap.get(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
            if (Objects.isNull(defaultPayParams)) {
                return Triple.of(false, "100442", "默认支付配置不存在，请先添加默认配置");
            }
            
            if (Objects.isNull(defaultPayParams)) {
                return Triple.of(false, "100442", "默认支付配置不存在，请先添加默认配置");
            }
            
            if (!Objects.equals(defaultPayParams.getAppId(), query.getAppId())) {
                return Triple.of(false, "100443", "用户端小程序appid错误");
            }
            
            if (existMap.containsKey(query.getFranchiseeId())) {
                return Triple.of(false, "100444", "加盟商配置已存在，请勿重复添加");
            }
        }
        
        return Triple.of(true, "", "");
    }
    
    @Override
    public Triple<Boolean, String, Object> modify(AlipayAppConfigQuery query) {
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId, String.valueOf(System.currentTimeMillis()), 5 * 1000L, true)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        AlipayAppConfig alipayAppConfig = alipayAppConfigMapper.selectById(query.getId());
        if (Objects.isNull(alipayAppConfig) || !Objects.equals(alipayAppConfig.getTenantId(), tenantId)) {
            return Triple.of(false, "100445", "支付配置不存在");
        }
        
        AlipayAppConfig alipayAppConfigOld = alipayAppConfigMapper.selectBySellerIdAndTenantId(query.getSellerId(), tenantId);
        if (Objects.nonNull(alipayAppConfigOld) && !Objects.equals(alipayAppConfigOld.getSellerId(), query.getSellerId())) {
            return Triple.of(false, "100440", "卖家支付宝账号与现有支付配置重复，请修改后操作");
        }
        
        Franchisee franchisee = null;
        if (AliPayConfigTypeEnum.FRANCHISEE_CONFIG.getType().equals(query.getConfigType())) {
            franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                return Triple.of(false, "100106", "加盟商不存在");
            }
        }
        
        //获取所有需要同步的加盟商配置
        List<Long> syncAlipayAppConfigIds = null;
        List<AlipayAppConfig> syncFranchiseeAlipayAppConfig = listSyncFranchiseeAlipayAppConfig(alipayAppConfigOld, query);
        if (CollectionUtils.isNotEmpty(syncFranchiseeAlipayAppConfig)) {
            syncAlipayAppConfigIds = syncFranchiseeAlipayAppConfig.stream().map(AlipayAppConfig::getId).collect(Collectors.toList());
        }
        
        AlipayAppConfig alipayAppConfigUpdate = new AlipayAppConfig();
        BeanUtils.copyProperties(query, alipayAppConfigUpdate);
        alipayAppConfigUpdate.setUpdateTime(System.currentTimeMillis());
        alipayAppConfigMapper.update(alipayAppConfigUpdate);
        
        if (CollectionUtils.isNotEmpty(syncAlipayAppConfigIds)) {
            alipayAppConfigMapper.updateSyncByIds(alipayAppConfigUpdate, syncAlipayAppConfigIds);
        }
        
        // 删除缓存
        List<String> delKeys = Optional.ofNullable(syncFranchiseeAlipayAppConfig).orElse(Collections.emptyList()).stream()
                .map(v -> buildCacheKey(v.getTenantId(), v.getFranchiseeId())).collect(Collectors.toList());
        delKeys.add(buildCacheKey(alipayAppConfigUpdate.getTenantId(), alipayAppConfigUpdate.getFranchiseeId()));
        
        redisService.delete(delKeys);
        
        this.operateRecord(Objects.isNull(franchisee) ? "" : franchisee.getName());
        
        return Triple.of(true, "", "");
    }
    
    private List<AlipayAppConfig> listSyncFranchiseeAlipayAppConfig(AlipayAppConfig alipayAppConfigOld, AlipayAppConfigQuery query) {
        //是否是默认配置变更
        if (!AliPayConfigTypeEnum.DEFAULT_CONFIG.getType().equals(query.getConfigType())) {
            return null;
        }
        
        //默认配置是否变更
        if (Objects.equals(alipayAppConfigOld.getAppId(), query.getAppId()) && Objects.equals(alipayAppConfigOld.getPublicKey(), query.getPublicKey()) && Objects
                .equals(alipayAppConfigOld.getAppPrivateKey(), query.getAppPrivateKey())) {
            return null;
        }
        
        return alipayAppConfigMapper.selectByConfigType(alipayAppConfigOld.getTenantId(), AliPayConfigTypeEnum.FRANCHISEE_CONFIG.getType());
    }
    
    @Override
    public Triple<Boolean, String, Object> remove(Long id) {
        Integer tenantId = TenantContextHolder.getTenantId();
        if (!redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId, String.valueOf(System.currentTimeMillis()), 5 * 1000L, true)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        AlipayAppConfig alipayAppConfig = alipayAppConfigMapper.selectById(id);
        if (Objects.isNull(alipayAppConfig) || !Objects.equals(alipayAppConfig.getTenantId(), tenantId)) {
            return Triple.of(false, "100445", "支付配置不存在");
        }
        
        if (AliPayConfigTypeEnum.FRANCHISEE_CONFIG.getType().equals(alipayAppConfig.getConfigType())) {
            return Triple.of(false, "100446", "默认配置不可删除");
        }
        
        AlipayAppConfig alipayAppConfigUpdate = new AlipayAppConfig();
        alipayAppConfigUpdate.setId(alipayAppConfig.getId());
        alipayAppConfigUpdate.setDelFlag(CommonConstant.DEL_Y);
        alipayAppConfigUpdate.setUpdateTime(System.currentTimeMillis());
        alipayAppConfigMapper.update(alipayAppConfigUpdate);
        
        redisService.delete(buildCacheKey(tenantId, alipayAppConfig.getFranchiseeId()));
        
        // 操作记录
        Franchisee franchisee = franchiseeService.queryByIdFromCache(alipayAppConfig.getFranchiseeId());
        this.operateRecord(Objects.nonNull(franchisee) ? franchisee.getName() : "");
        
        return Triple.of(true, "", "");
    }
    
    @Slave
    @Override
    public AlipayAppConfig queryByAppId(String appId) {
        return this.alipayAppConfigMapper.selectByAppId(appId);
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
    
    @Override
    public AlipayAppConfig queryByTenantId(Integer tenantId) {
        return alipayAppConfigMapper.selectOneByTenantId(tenantId);
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
    
    private void operateRecord(String franchiseeName) {
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("franchiseeName", franchiseeName);
        operateRecordUtil.record(null, record);
    }
    
    /**
     * 构建缓存key
     */
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(CacheConstant.ELE_ALI_PAY_PARAMS_KEY, tenantId, franchiseeId);
    }
}
