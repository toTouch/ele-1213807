package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.pay.WechatPublicKeyBO;
import com.xiliulou.electricity.config.WechatConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.converter.ElectricityPayParamsConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.entity.WechatWithdrawalCertificate;
import com.xiliulou.electricity.enums.ElectricityPayParamsConfigEnum;
import com.xiliulou.electricity.mapper.ElectricityPayParamsMapper;
import com.xiliulou.electricity.query.FranchiseeQuery;
import com.xiliulou.electricity.queryModel.WechatPublicKeyQueryModel;
import com.xiliulou.electricity.request.payparams.ElectricityPayParamsRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.WechatPaymentCertificateService;
import com.xiliulou.electricity.service.WechatWithdrawalCertificateService;
import com.xiliulou.electricity.service.pay.WechatPublicKeyService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.transaction.ElectricityPayParamsTxService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.ElectricityPayParamsVO;
import com.xiliulou.electricity.vo.FranchiseeIdNameVO;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xiliulou.electricity.constant.MultiFranchiseeConstant.DEFAULT_FRANCHISEE_NAME;

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
    
    
    @Resource
    private ProfitSharingConfigService profitSharingConfigService;
    
    @Resource
    private WechatPublicKeyService wechatPublicKeyService;
    
    @Override
    public R insert(ElectricityPayParamsRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        request.setTenantId(tenantId);
        
        //加锁
        if (!idempotentCheck()) {
            return R.failMsg("操作频繁!");
        }
        String franchiseeName = DEFAULT_FRANCHISEE_NAME;
        if (!ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(request.getConfigType())) {
            Franchisee franchisee = this.queryFranchisee(request.getTenantId(), request.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                return R.failMsg("加盟商不存在");
            }
            franchiseeName = franchisee.getName();
        } else {
            // 默认配置
            request.setFranchiseeId(MultiFranchiseeConstant.DEFAULT_FRANCHISEE);
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
        if (Objects.nonNull(request.getPubKeyId())){
            saveWechatPublicKeyId(request,tenantId, ObjectUtils.defaultIfNull(insert.getId(),NumberConstant.NEGATIVE_ONE).longValue());
        }
        // 缓存删除
        redisService.delete(buildCacheKey(tenantId, insert.getFranchiseeId()));
        
        // 操作记录
        this.operateRecord(franchiseeName);
        
        return R.ok();
    }
    
    
    @Override
    public R update(ElectricityPayParamsRequest request) {
        
        if (!this.idempotentCheck()) {
            return R.failMsg("操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        request.setTenantId(tenantId);
        // 校验参数
        ElectricityPayParams oldPayParams = baseMapper.selectOne(
                new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getId, request.getId()).eq(ElectricityPayParams::getTenantId, request.getTenantId()));
        if (Objects.isNull(oldPayParams)) {
            return R.failMsg("数据不存在");
        }
        
        // 校验微信商户号
        ElectricityPayParams payParams = baseMapper.selectByTenantIdAndWechatMerchantId(tenantId, request.getWechatMerchantId());
        if (Objects.nonNull(payParams) && !Objects.equals(payParams.getId(), oldPayParams.getId())) {
            return R.failMsg("微信商户号与现有支付配置重复，请修改后操作");
        }
        
        String franchiseeName = DEFAULT_FRANCHISEE_NAME;
        if (!ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(oldPayParams.getConfigType())) {
            Franchisee franchisee = this.queryFranchisee(tenantId, oldPayParams.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                return R.failMsg("加盟商不存在");
            }
            franchiseeName = franchisee.getName();
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
        
        if (Objects.nonNull(request.getPubKeyId())){
            saveWechatPublicKeyId(request,tenantId,ObjectUtils.defaultIfNull(update.getId(),NumberConstant.NEGATIVE_ONE).longValue());
        }
        
        // 删除缓存
        List<String> delKeys = Optional.ofNullable(syncFranchiseePayParam).orElse(Collections.emptyList()).stream().map(v -> buildCacheKey(v.getTenantId(), v.getFranchiseeId()))
                .collect(Collectors.toList());
        delKeys.add(buildCacheKey(update.getTenantId(), update.getFranchiseeId()));
        
        redisService.delete(delKeys);
        
        this.operateRecord(franchiseeName);
        return R.ok();
    }
    
    
    @Override
    public R delete(Long id) {
        
        if (!this.idempotentCheck()) {
            return R.failMsg("操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 校验参数
        ElectricityPayParams payParams = baseMapper
                .selectOne(new LambdaQueryWrapper<ElectricityPayParams>().eq(ElectricityPayParams::getId, id).eq(ElectricityPayParams::getTenantId, tenantId));
        
        if (Objects.isNull(payParams)) {
            return R.failMsg("数据不存在");
        }
        
        if (ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(payParams.getConfigType())) {
            return R.failMsg("默认配置不可删除");
        }
        
        // 逻辑删除分账配置：
        profitSharingConfigService.removeByPayParamId(tenantId, payParams.getId());
        
        // 逻辑删除
        electricityPayParamsTxService.delete(id, tenantId);
        
        WechatPublicKeyBO publicKeyBO = wechatPublicKeyService.queryByTenantIdFromCache(tenantId.longValue(), payParams.getFranchiseeId());
        if (Objects.nonNull(publicKeyBO)){
            wechatPublicKeyService.delete(publicKeyBO.getId());
        }
        
        // 缓存删除
        redisService.delete(buildCacheKey(tenantId, payParams.getFranchiseeId()));
        wechatPaymentCertificateService.deleteCache(tenantId, payParams.getFranchiseeId());
        
        // 操作记录
        Franchisee franchisee = this.queryFranchisee(tenantId, payParams.getFranchiseeId());
        this.operateRecord(Objects.nonNull(franchisee) ? franchisee.getName() : "");
        return R.ok();
    }
    
    @Slave
    @Override
    public List<ElectricityPayParamsVO> queryByTenantId(Integer tenantId) {
        List<ElectricityPayParams> params = baseMapper.selectByTenantId(tenantId);
        List<ElectricityPayParamsVO> voList = ElectricityPayParamsConverter.qryDoToVos(params);
        this.buildFranchiseeName(tenantId, voList);
        if (CollectionUtils.isEmpty(voList)){
            return List.of();
        }
        
        List<Long> franchiseeIds = voList.stream().map(ElectricityPayParamsVO::getFranchiseeId).filter(Objects::nonNull).collect(Collectors.toList());
        List<WechatPublicKeyBO> publicKeyBOS = wechatPublicKeyService.queryListByTenantIdFromCache(tenantId.longValue(), franchiseeIds);
        if (CollectionUtils.isEmpty(publicKeyBOS)){
            return voList;
        }
        
        Map<Long, WechatPublicKeyBO> publicKeyBOMap = publicKeyBOS.stream().collect(Collectors.toMap(WechatPublicKeyBO::getPayParamsId, Function.identity()));
        voList.forEach(vo -> vo.setPubKeyId(publicKeyBOMap.get(vo.getId().longValue()).getPubKeyId()));
        
        return voList;
    }
    
    
    @Slave
    @Override
    public List<FranchiseeIdNameVO> queryFranchisee(Integer tenantId, List<Long> dataPermissionFranchiseeIds) {
        List<Long> franchiseeIds = baseMapper.selectFranchiseeIdsByTenantId(tenantId);
        // 过滤掉默认加盟商
        franchiseeIds = Optional.ofNullable(franchiseeIds).orElse(Collections.emptyList()).stream().filter(v -> !MultiFranchiseeConstant.DEFAULT_FRANCHISEE.equals(v)).distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            return Collections.emptyList();
        }
        
        if (CollectionUtils.isNotEmpty(dataPermissionFranchiseeIds)) {
            franchiseeIds = franchiseeIds.stream().filter(v -> dataPermissionFranchiseeIds.contains(v)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(franchiseeIds)) {
                return Collections.emptyList();
            }
            
        }
        
        List<Franchisee> franchisees = franchiseeService.queryByIds(franchiseeIds, tenantId);
        if (CollectionUtils.isEmpty(franchisees)) {
            return Collections.emptyList();
        }
        
        return franchisees.stream().map(franchisee -> {
            FranchiseeIdNameVO franchiseeVO = new FranchiseeIdNameVO();
            franchiseeVO.setId(franchisee.getId());
            franchiseeVO.setName(franchisee.getName());
            return franchiseeVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public ElectricityPayParams queryByWechatMerchantId(Integer tenantId, String wechatMerchantId) {
        return baseMapper.selectByTenantIdAndWechatMerchantId(tenantId, wechatMerchantId);
    }
    
    
    
    @Override
    public R uploadFile(MultipartFile file, Integer type, Long franchiseeId) {
        Integer tenantId = TenantContextHolder.getTenantId();
        //加锁
        if (!this.idempotentCheck()) {
            return R.failMsg("操作频繁!");
        }
        try {
            
            ElectricityPayParams oldElectricityPayParams = queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            if (Objects.isNull(oldElectricityPayParams)) {
                return R.fail("找不到支付配置");
            }
            
            ElectricityPayParams electricityPayParams = new ElectricityPayParams();
            electricityPayParams.setId(oldElectricityPayParams.getId());
            electricityPayParams.setTenantId(tenantId);
            electricityPayParams.setUpdateTime(System.currentTimeMillis());
            
            String franchiseeName = DEFAULT_FRANCHISEE_NAME;
            if (!ElectricityPayParamsConfigEnum.DEFAULT_CONFIG.getType().equals(oldElectricityPayParams.getConfigType())) {
                Franchisee franchisee = this.queryFranchisee(tenantId, franchiseeId);
                franchiseeName = Objects.nonNull(franchisee) ? franchisee.getName() : "";
            }
            
            if (Objects.isNull(type) || Objects.equals(type, ElectricityPayParams.TYPE_MERCHANT_PATH)) {
                WechatPaymentCertificate wechatPaymentCertificate = new WechatPaymentCertificate();
                wechatPaymentCertificate.setTenantId(tenantId);
                wechatPaymentCertificate.setPayParamsId(oldElectricityPayParams.getId());
                wechatPaymentCertificate.setFranchiseeId(franchiseeId);
                wechatPaymentCertificateService.handleCertificateFile(file, wechatPaymentCertificate);
                Map<String, Object> recordMap = Maps.newHashMapWithExpectedSize(2);
                recordMap.put("franchiseeName", franchiseeName);
                recordMap.put("watchUploadType", 1);
                operateRecordUtil.record(null, recordMap);
            } else {
                WechatWithdrawalCertificate wechatWithdrawalCertificate = new WechatWithdrawalCertificate();
                wechatWithdrawalCertificate.setTenantId(tenantId);
                wechatWithdrawalCertificate.setPayParamsId(oldElectricityPayParams.getId());
                wechatWithdrawalCertificate.setFranchiseeId(franchiseeId);
                Map<String, Object> recordMap = Maps.newHashMapWithExpectedSize(2);
                recordMap.put("franchiseeName", franchiseeName);
                recordMap.put("watchUploadType", 2);
                operateRecordUtil.record(null, recordMap);
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
    
    @Override
    public ElectricityPayParams queryPreciseCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) {
        List<ElectricityPayParams> electricityPayParamsList = this.queryFromCacheList(tenantId, Sets.newHashSet(franchiseeId));
        if (CollectionUtils.isEmpty(electricityPayParamsList)) {
            return null;
        }
        return electricityPayParamsList.get(0);
    }
    
    @Override
    public List<ElectricityPayParams> queryListPreciseCacheByTenantIdAndFranchiseeId(Integer tenantId, Set<Long> franchiseeIds) {
        return this.queryFromCacheList(tenantId, franchiseeIds);
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
    
    private void saveWechatPublicKeyId(ElectricityPayParamsRequest request, Integer tenantId , Long payParamsId){
        if (Objects.nonNull(request.getPubKeyId())){
            WechatPublicKeyBO bo = wechatPublicKeyService.queryByTenantIdFromCache(tenantId.longValue(), request.getFranchiseeId());
            if (Objects.isNull(bo)){
                WechatPublicKeyBO insert = WechatPublicKeyBO.builder()
                        .payParamsId(ObjectUtils.defaultIfNull(payParamsId, NumberConstant.NEGATIVE_ONE))
                        .tenantId(tenantId.longValue())
                        .franchiseeId(request.getFranchiseeId())
                        .pubKeyId(StringUtils.defaultIfEmpty(request.getPubKeyId(), StringConstant.EMPTY))
                        .uploadTime(NumberConstant.NEGATIVE_ONE)
                        .pubKey(StringConstant.EMPTY)
                        .build();
                wechatPublicKeyService.save(insert);
                return;
            }
            bo.setPayParamsId(ObjectUtils.defaultIfNull(payParamsId, NumberConstant.NEGATIVE_ONE));
            bo.setPubKeyId(StringUtils.defaultIfEmpty(request.getPubKeyId(), StringConstant.EMPTY));
            wechatPublicKeyService.update(bo);
        }
    }
    
    
    /**
     * 幂等性校验
     */
    private Boolean idempotentCheck() {
        // 幂等由20秒 改为 5秒
        return redisService.setNx(CacheConstant.ADMIN_OPERATE_LOCK_KEY + TenantContextHolder.getTenantId(), String.valueOf(System.currentTimeMillis()), 5 * 1000L, true);
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
     * @param franchiseeName
     * @author caobotao.cbt
     * @date 2024/6/14 14:56
     */
    private void operateRecord(String franchiseeName) {
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("franchiseeName", franchiseeName);
        operateRecordUtil.record(null, record);
    }
    
    /**
     * 加盟商名称构建
     *
     * @param tenantId
     * @param voList
     * @author caobotao.cbt
     * @date 2024/6/17 17:29
     */
    private void buildFranchiseeName(Integer tenantId, List<ElectricityPayParamsVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return;
        }
        
        List<Long> franchiseeIds = voList.stream().filter(v -> ElectricityPayParamsConfigEnum.FRANCHISEE_CONFIG.getType().equals(v.getConfigType()))
                .map(ElectricityPayParamsVO::getFranchiseeId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(franchiseeIds)) {
            return;
        }
        
        FranchiseeQuery franchiseeQuery = new FranchiseeQuery();
        franchiseeQuery.setTenantId(tenantId);
        franchiseeQuery.setIds(franchiseeIds);
        Triple<Boolean, String, Object> franchiseeTriple = franchiseeService.selectListByQuery(franchiseeQuery);
        if (!franchiseeTriple.getLeft()) {
            return;
        }
        
        List<Franchisee> franchiseeList = (List<Franchisee>) franchiseeTriple.getRight();
        if (CollectionUtils.isEmpty(franchiseeList)) {
            return;
        }
        
        Map<Long, String> franchiseeMap = franchiseeList.stream().collect(Collectors.toMap(Franchisee::getId, v -> v.getName(), (k1, k2) -> k1));
        
        voList.stream().filter(v -> ElectricityPayParamsConfigEnum.FRANCHISEE_CONFIG.getType().equals(v.getConfigType())).forEach(vo -> {
            vo.setFranchiseeName(franchiseeMap.get(vo.getFranchiseeId()));
        });
        
    }
    
}
