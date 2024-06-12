package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.ElectricityMerchantProConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 11:36
 **/
@Service
@Slf4j
public class ElectricityPayParamsServiceImpl extends ServiceImpl<ElectricityPayParamsMapper, ElectricityPayParams> implements ElectricityPayParamsService {
    
    @Resource
    private TenantService tenantService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    WechatConfig config;
    
    @Autowired
    private WechatPaymentCertificateService wechatPaymentCertificateService;
    
    @Autowired
    private WechatWithdrawalCertificateService wechatWithdrawalCertificateService;
    
    /**
     * 新增或修改
     *
     * @param electricityPayParams
     * @return
     */
    @Override
    public R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams) {
        Integer tenantId = TenantContextHolder.getTenantId();
        //加锁
        Boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId, String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }
        
        ElectricityPayParams oldElectricityPayParams1 = queryFromCache(tenantId);
        
        ElectricityPayParams oldElectricityPayParams2 = baseMapper
                .selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, electricityPayParams.getMerchantMinProAppId()));
        
        if (Objects.nonNull(oldElectricityPayParams2)) {
            if (Objects.isNull(oldElectricityPayParams1) || !Objects.equals(oldElectricityPayParams1.getId(), oldElectricityPayParams2.getId())) {
                return R.failMsg("该小程序appId已被使用，请勿重复使用!");
            }
        }
        
        electricityPayParams.setUpdateTime(System.currentTimeMillis());
        if (Objects.isNull(oldElectricityPayParams1)) {
            electricityPayParams.setCreateTime(System.currentTimeMillis());
            electricityPayParams.setTenantId(tenantId);
            baseMapper.insert(electricityPayParams);
        } else {
            if (ObjectUtil.notEqual(oldElectricityPayParams1.getId(), electricityPayParams.getId())) {
                return R.fail("请求参数id,不合法!");
            }
            redisService.delete(CacheConstant.CACHE_PAY_PARAMS + tenantId);
            baseMapper.updateById(electricityPayParams);
        }
        redisService.delete(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId);
        return R.ok();
    }
    
    @Override
    public R insert(ElectricityPayParamsRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        request.setTenantId(tenantId);
        
        //加锁
        Boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId, String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }
        // 校验参数
        String msg = this.insertCheck(request);
        if (StringUtils.isNotBlank(msg)) {
            return R.failMsg(msg);
        }
        
        //新增
        ElectricityPayParams insert = ElectricityPayParamsConverter.optRequestToDO(request);
        baseMapper.insert(insert);
        // 缓存删除
        this.deleteCache(tenantId, insert.getFranchiseeId());
        return R.ok();
    }
    
    
    /**
     * 获取支付参数 valid_days
     *
     * @return
     */
    @Override
    public ElectricityPayParams queryFromCache(Integer tenantId) {
        // TODO: 2024/6/12 CBT后续删除
        return null;
    }
    
    @Override
    public R uploadFile(MultipartFile file, Integer type) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //加锁
        boolean getLockerSuccess = redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId, String.valueOf(System.currentTimeMillis()), 3 * 1000L, true);
        if (!getLockerSuccess) {
            return R.failMsg("操作频繁!");
        }
        try {
            ElectricityPayParams oldElectricityPayParams = queryFromCache(tenantId);
            if (Objects.isNull(oldElectricityPayParams)) {
                return R.fail("找不到支付配置");
            }
            
            ElectricityPayParams electricityPayParams = new ElectricityPayParams();
            electricityPayParams.setId(oldElectricityPayParams.getId());
            electricityPayParams.setTenantId(tenantId);
            electricityPayParams.setUpdateTime(System.currentTimeMillis());
            if (Objects.isNull(type) || Objects.equals(type, ElectricityPayParams.TYPE_MERCHANT_PATH)) {
                wechatPaymentCertificateService.handleCertificateFile(file, tenantId);
            } else {
                wechatWithdrawalCertificateService.handleCertificateFile(file, tenantId);
            }
            //更新支付参数
            updateElectricityPayParams(electricityPayParams);
        } catch (Exception e) {
            log.error("certificate get error, tenantId={}", tenantId);
            return R.fail("证书内容获取失败，请重试！");
        } finally {
            //解锁
            redisService.remove(CacheConstant.ADMIN_OPERATE_LOCK_KEY + tenantId);
        }
        return R.ok();
        
    }
    
    
    @Override
    public ElectricityPayParams selectTenantId(String appId) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
    }
    
    @Override
    public Triple<Boolean, String, Object> queryByMerchantAppId(String appId) {
        ElectricityPayParams electricityPayParams = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantAppletId, appId));
        if (Objects.isNull(electricityPayParams)) {
            return Triple.of(false, null, "未能发现相关的商户小程序配置");
        }
        
        Integer tenantId = electricityPayParams.getTenantId();
        ElectricityMerchantProConfigVO vo = new ElectricityMerchantProConfigVO();
        vo.setTenantId(tenantId);
        
        // 获取租户编码
        Tenant tenant = tenantService.queryByIdFromCache(tenantId);
        vo.setTenantCode(ObjectUtils.isNotEmpty(tenant) ? tenant.getCode() : null);
        
        // 获取客服电话
        String servicePhone = redisService.get(CacheConstant.CACHE_SERVICE_PHONE + tenantId);
        vo.setServicePhone(servicePhone);
        
        return Triple.of(true, null, vo);
    }
    
    @Override
    public ElectricityPayParams queryCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) {
        
        // 批量查询缓存
        List<ElectricityPayParams> electricityPayParamsList = this.queryFromCacheList(tenantId, Sets.newHashSet(franchiseeId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE));
        if (CollectionUtils.isEmpty(electricityPayParamsList)) {
            log.warn("WARN! WeChat Pay parameter is not configured,tenantId={},franchiseeId={}", tenantId, franchiseeId);
            return null;
        }
        
        Map<Long, ElectricityPayParams> payParamsMap = electricityPayParamsList.stream().collect(Collectors.toMap(ElectricityPayParams::getFranchiseeId, v -> v, (k1, k2) -> k1));
        
        // 先取加盟商配置
        ElectricityPayParams payParams = payParamsMap.get(franchiseeId);
        
        if (Objects.isNull(payParams)) {
            // 加盟商未配置 取默认配置
            payParams = payParamsMap.get(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
        }
        
        return payParams;
    }
    
    /**
     * 批量查询缓存
     *
     * @param tenantId
     * @param franchiseeIdSet
     * @author caobotao.cbt
     * @date 2024/6/12 13:38
     */
    private List<ElectricityPayParams> queryFromCacheList(Integer tenantId, Set<Long> franchiseeIdSet) {
        
        // 从缓存中获取数据
        List<String> cacheKeys = franchiseeIdSet.stream().map(franchiseeId -> buildCacheKey(tenantId, franchiseeId)).collect(Collectors.toList());
        List<ElectricityPayParams> cacheList = redisService.multiJsonGet(cacheKeys, ElectricityPayParams.class);
        
        // key：franchiseeId
        Map<Long, ElectricityPayParams> cacheFranchiseeIdMap = Optional.ofNullable(cacheList).orElse(Collections.emptyList()).stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(ElectricityPayParams::getFranchiseeId, Function.identity(), (k1, k2) -> k1));
        
        // 查询数据库的集合
        List<Long> qryDbList = new ArrayList<>(franchiseeIdSet.size());
        
        List<ElectricityPayParams> payParams = new ArrayList<>(franchiseeIdSet.size());
        
        franchiseeIdSet.forEach(fid -> {
            ElectricityPayParams cache = cacheFranchiseeIdMap.get(fid);
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
        
        // 如果需要，从数据库查询数据
        List<ElectricityPayParams> dbList = baseMapper.selectListByTenantIdAndFranchiseeIds(tenantId, qryDbList);
        
        if (CollectionUtils.isNotEmpty(dbList)) {
            // 添加到结果集
            payParams.addAll(dbList);
        }
        
        // 更新缓存
        Map<String, String> cacheSaveMap = Optional.ofNullable(dbList).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(e -> buildCacheKey(e.getTenantId(), e.getFranchiseeId()), v -> JsonUtil.toJson(v), (k1, k2) -> k1));
        
        // 处理不存在的数据
        qryDbList.stream().filter(franchiseeId -> !cacheSaveMap.containsKey(buildCacheKey(tenantId, franchiseeId))).forEach(franchiseeId -> {
            ElectricityPayParams nullParam = new ElectricityPayParams();
            nullParam.setTenantId(tenantId);
            nullParam.setFranchiseeId(franchiseeId);
            cacheSaveMap.put(buildCacheKey(tenantId, franchiseeId), JsonUtil.toJson(nullParam));
        });
        
        // 批量设置缓存
        redisService.multiSet(cacheSaveMap);
        
        return payParams;
    }
    
    /**
     * 更新支付参数
     *
     * @param electricityPayParams electricityPayParams
     */
    private void updateElectricityPayParams(ElectricityPayParams electricityPayParams) {
        baseMapper.updateById(electricityPayParams);
        redisService.delete(CacheConstant.CACHE_PAY_PARAMS + electricityPayParams.getTenantId());
    }
    
    
    /**
     * 构建缓存key
     */
    private String buildCacheKey(Integer tenantId, Long franchiseeId) {
        return String.format(CacheConstant.ELE_PAY_PARAMS_KEY, tenantId, franchiseeId);
    }
    
    
    /**
     * 新增校验
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/12 16:46
     */
    private String insertCheck(ElectricityPayParamsRequest request) {
        
        Integer tenantId = request.getTenantId();
        Long franchiseeId = request.getFranchiseeId();
        Integer configType = request.getConfigType();
        
        if (ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(configType)) {
            // 默认配置
            // 查询默认配置是否存在
            List<ElectricityPayParams> electricityPayParams = queryFromCacheList(tenantId, Collections.singleton(MultiFranchiseeConstant.DEFAULT_FRANCHISEE));
            if (CollectionUtils.isNotEmpty(electricityPayParams)) {
                return "默认配置已存在";
            }
        } else {
            // 运营商配置
            if (Objects.isNull(franchiseeId)) {
                return "加盟商不能为空";
            }
            List<ElectricityPayParams> electricityPayParams = queryFromCacheList(tenantId, Sets.newHashSet(franchiseeId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE));
            Map<Long, ElectricityPayParams> franchiseeParamsMap = Optional.ofNullable(electricityPayParams).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(ElectricityPayParams::getFranchiseeId, v -> v, (k1, k2) -> k1));
            if (!franchiseeParamsMap.containsKey(MultiFranchiseeConstant.DEFAULT_FRANCHISEE)) {
                return "默认配置不存在";
            }
            
            if (franchiseeParamsMap.containsKey(franchiseeId)) {
                return "加盟商配置已存在";
            }
        }
        return null;
    }
    
    
    /**
     * 缓存删除
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 16:57
     */
    private void deleteCache(Integer tenantId, Long franchiseeId) {
        redisService.delete(buildCacheKey(tenantId, franchiseeId));
    }
}
