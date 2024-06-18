package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import com.xiliulou.electricity.service.transaction.ElectricityPayParamsTxService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    WechatConfig config;
    
    @Autowired
    OperateRecordUtil operateRecordUtil;
    
    @Autowired
    private WechatPaymentCertificateService wechatPaymentCertificateService;
    
    @Autowired
    private WechatWithdrawalCertificateService wechatWithdrawalCertificateService;
    
    @Autowired
    private ElectricityPayParamsTxService electricityPayParamsTxService;
    
    /**
     * 新增或修改
     *
     * @param electricityPayParams
     * @return
     */
    @Override
    public R saveOrUpdateElectricityPayParams(ElectricityPayParams electricityPayParams) {
        // TODO: 2024/6/12 CBT后续删除
        return R.ok();
    }
    
    @Override
    public R insert(ElectricityPayParamsRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        request.setTenantId(tenantId);
        
        //加锁
        if (!idempotentCheck()) {
            return R.failMsg("操作频繁!");
        }
        
        Franchisee franchisee = this.queryFranchisee(request.getTenantId(), request.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return R.failMsg("加盟商不存在");
        }
        
        // 校验参数
        String msg = this.insertCheck(request);
        if (StringUtils.isNotBlank(msg)) {
            return R.failMsg(msg);
        }
        
        //新增
        ElectricityPayParams insert = ElectricityPayParamsConverter.optRequestToDO(request);
        insert.setCreateTime(System.currentTimeMillis());
        insert.setUpdateTime(System.currentTimeMillis());
        baseMapper.insert(insert);
        // 缓存删除
        redisService.delete(buildCacheKey(tenantId, insert.getFranchiseeId()));
        
        // 操作记录
        this.operateRecord(franchisee);
        
        return R.ok();
    }
    
    
    @Override
    public R update(ElectricityPayParamsRequest request) {
        
        if (!this.idempotentCheck()) {
            return R.failMsg("操作频繁");
        }
        
        Franchisee franchisee = this.queryFranchisee(request.getTenantId(), request.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return R.failMsg("加盟商不存在");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        request.setTenantId(tenantId);
        // 校验参数
        ElectricityPayParams oldPayParams = baseMapper.selectOne(
                new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getId, request.getId()).eq(ElectricityPayParams::getTenantId, request.getTenantId()));
        if (Objects.isNull(oldPayParams)) {
            return R.failMsg("数据不存在");
        }
        
        // 需要同步的加盟商配置
        List<ElectricityPayParams> syncFranchiseePayParam = this.getSyncFranchiseePayParam(oldPayParams, request);
        List<Integer> franchiseePayParamIds = null;
        if (CollectionUtils.isNotEmpty(syncFranchiseePayParam)) {
            franchiseePayParamIds = syncFranchiseePayParam.stream().map(ElectricityPayParams::getId).collect(Collectors.toList());
        }
        
        //更新
        ElectricityPayParams update = ElectricityPayParamsConverter.optRequestToDO(request);
        
        electricityPayParamsTxService.update(update, franchiseePayParamIds);
        
        // 删除缓存
        List<String> delKeys = Optional.ofNullable(syncFranchiseePayParam).orElse(Collections.emptyList()).stream().map(v -> buildCacheKey(v.getTenantId(), v.getFranchiseeId()))
                .collect(Collectors.toList());
        delKeys.add(buildCacheKey(update.getTenantId(), update.getFranchiseeId()));
        
        redisService.delete(delKeys);
        
        this.operateRecord(franchisee);
        return R.ok();
    }
    
    
    @Override
    public R delete(Long id) {
        
        if (!this.idempotentCheck()) {
            return R.failMsg("操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 校验参数
        ElectricityPayParams payParams = baseMapper.selectOne(
                new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getId, id).eq(ElectricityPayParams::getTenantId, tenantId));
        
        if (Objects.isNull(payParams)) {
            return R.failMsg("数据不存在");
        }
        
        if (ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(payParams.getConfigType())) {
            return R.failMsg("默认配置不可删除");
        }
        
        // 逻辑删除
        electricityPayParamsTxService.delete(id, tenantId);
        
        // 缓存删除
        redisService.delete(buildCacheKey(tenantId, payParams.getFranchiseeId()));
        wechatPaymentCertificateService.deleteCache(tenantId, payParams.getFranchiseeId());
        
        // 操作记录
        Franchisee franchisee = this.queryFranchisee(tenantId, payParams.getFranchiseeId());
        this.operateRecord(franchisee);
        return R.ok();
    }
    
    @Slave
    @Override
    public List<ElectricityPayParamsVO> queryByTenantId(Integer tenantId) {
        List<ElectricityPayParams> params = baseMapper.selectByTenantId(tenantId);
        List<ElectricityPayParamsVO> voList = ElectricityPayParamsConverter.qryDoToVos(params);
        return voList;
    }
    
    @Override
    public R getTenantId(String appId) {
        // TODO: 2024/6/17 web未找到对应的调用页面,暂时兼容愿逻辑
        //        ElectricityPayParams electricityPayParams = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
        List<ElectricityPayParams> electricityPayParams = baseMapper
                .selectList(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
        if (CollectionUtils.isEmpty(electricityPayParams)) {
            return R.fail("ELECTRICITY.00101", "找不到租户");
        }
        return R.ok(electricityPayParams.get(0).getTenantId());
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
    public R uploadFile(MultipartFile file, Integer type, Long franchiseeId) {
        Integer tenantId = TenantContextHolder.getTenantId();
        //加锁
        if (!this.idempotentCheck()) {
            return R.failMsg("操作频繁!");
        }
        try {
            
            ElectricityPayParams oldElectricityPayParams = queryCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(oldElectricityPayParams)) {
                return R.fail("找不到支付配置");
            }
            
            ElectricityPayParams electricityPayParams = new ElectricityPayParams();
            electricityPayParams.setId(oldElectricityPayParams.getId());
            electricityPayParams.setTenantId(tenantId);
            electricityPayParams.setUpdateTime(System.currentTimeMillis());
            if (Objects.isNull(type) || Objects.equals(type, ElectricityPayParams.TYPE_MERCHANT_PATH)) {
                WechatPaymentCertificate wechatPaymentCertificate = new WechatPaymentCertificate();
                wechatPaymentCertificate.setTenantId(tenantId);
                wechatPaymentCertificate.setPayParamsId(oldElectricityPayParams.getId());
                wechatPaymentCertificate.setFranchiseeId(franchiseeId);
                wechatPaymentCertificateService.handleCertificateFile(file, wechatPaymentCertificate);
            } else {
                WechatWithdrawalCertificate wechatWithdrawalCertificate = new WechatWithdrawalCertificate();
                wechatWithdrawalCertificate.setTenantId(tenantId);
                wechatWithdrawalCertificate.setPayParamsId(oldElectricityPayParams.getId());
                wechatWithdrawalCertificate.setFranchiseeId(franchiseeId);
                wechatWithdrawalCertificateService.handleCertificateFile(file, wechatWithdrawalCertificate);
            }
            //更新支付参数
            baseMapper.update(electricityPayParams);
            // 缓存删除
            redisService.delete(buildCacheKey(tenantId, franchiseeId));
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
        //        return baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
        List<ElectricityPayParams> electricityPayParams = baseMapper
                .selectList(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantMinProAppId, appId));
        if (CollectionUtils.isEmpty(electricityPayParams)) {
            return null;
        }
        return electricityPayParams.get(0);
    }
    
    @Deprecated
    @Override
    public Triple<Boolean, String, Object> queryByMerchantAppId(String appId) {
        /*ElectricityPayParams electricityPayParams = baseMapper.selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getMerchantAppletId, appId));
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
        vo.setServicePhone(servicePhone);*/
        
        return Triple.of(true, null, null);
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
        
        // 从数据库查询数据
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
                return "默认配置已存在,请勿重复添加";
            }
        } else {
            // 运营商配置
            if (Objects.isNull(franchiseeId)) {
                return "加盟商不能为空";
            }
            List<ElectricityPayParams> electricityPayParams = queryFromCacheList(tenantId, Sets.newHashSet(franchiseeId, MultiFranchiseeConstant.DEFAULT_FRANCHISEE));
            Map<Long, ElectricityPayParams> franchiseeParamsMap = Optional.ofNullable(electricityPayParams).orElse(Collections.emptyList()).stream()
                    .collect(Collectors.toMap(ElectricityPayParams::getFranchiseeId, v -> v, (k1, k2) -> k1));
            ElectricityPayParams defaultPayParams = franchiseeParamsMap.get(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
            if (Objects.isNull(defaultPayParams)) {
                return "默认配置不存在，请先添加默认配置";
            }
            if (!Objects.equals(defaultPayParams.getMerchantMinProAppId(), request.getMerchantMinProAppId())) {
                return "用户端小程序appid错误";
            }
            
            if (!Objects.equals(defaultPayParams.getMerchantMinProAppSecert(), request.getMerchantMinProAppSecert())) {
                return "用户端小程序appsecert错误";
            }
            
            if (franchiseeParamsMap.containsKey(franchiseeId)) {
                return "加盟商配置已存在，请勿重复添加";
            }
        }
        
        // 校验微信商户号
        ElectricityPayParams payParams = baseMapper.selectByTenantIdAndWechatMerchantId(tenantId, request.getWechatMerchantId());
        if (Objects.nonNull(payParams)) {
            return "微信商户号与现有支付配置重复，请修改后操作";
        }
        
        return null;
    }
    
    
    /**
     * 幂等性校验
     */
    private Boolean idempotentCheck() {
        return redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + TenantContextHolder.getTenantId(), String.valueOf(System.currentTimeMillis()), 20 * 1000L, true);
    }
    
    
    /**
     * 获取需要同步的加盟商支付配置
     *
     * @param oldPayParams
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/13 17:04
     */
    private List<ElectricityPayParams> getSyncFranchiseePayParam(ElectricityPayParams oldPayParams, ElectricityPayParamsRequest request) {
        //是否是默认配置变更
        if (!ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(oldPayParams.getConfigType())) {
            return null;
        }
        
        // 默认配置的小程序appid 和 appSecert 是否变更
        if (Objects.equals(oldPayParams.getMerchantMinProAppId(), request.getMerchantMinProAppId()) && Objects
                .equals(oldPayParams.getMerchantMinProAppSecert(), request.getMerchantMinProAppSecert())) {
            return null;
        }
        
        // 小程序appid获取是小程序 Secert 有变更 则要同步所有的子配置
        return baseMapper.selectIdsByTenantIdAndConfigType(oldPayParams.getTenantId(), ElectricityPayParamsConfigEnum.FRANCHISEE_CONFIG.getType());
    }
    
    /**
     * 加盟商查询
     *
     * @author caobotao.cbt
     * @date 2024/6/14 14:43
     */
    private Franchisee queryFranchisee(Integer tenantId, Long franchiseeId) {
        Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            return null;
        }
        return franchisee;
    }
    
    
    /**
     * 操作记录
     *
     * @param franchisee
     * @author caobotao.cbt
     * @date 2024/6/14 14:56
     */
    private void operateRecord(Franchisee franchisee) {
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("franchiseeName", franchisee.getName());
        operateRecordUtil.record(null, record);
    }
    
}
