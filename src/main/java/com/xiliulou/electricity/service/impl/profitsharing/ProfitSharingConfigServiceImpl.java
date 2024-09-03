

package com.xiliulou.electricity.service.impl.profitsharing;

import java.math.BigDecimal;

import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MultiFranchiseeConstant;
import com.xiliulou.electricity.converter.profitsharing.ProfitSharingConfigConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.WechatPaymentCertificate;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigCycleTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigOrderTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigProfitSharingTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigStatusEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingConfigMapper;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigUpdateStatusOptRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingReceiverConfigService;
import com.xiliulou.electricity.tx.profitsharing.ProfitSharingConfigTxService;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/22 16:47
 */
@Slf4j
@Service
public class ProfitSharingConfigServiceImpl implements ProfitSharingConfigService {
    
    @Resource
    private ProfitSharingConfigMapper profitSharingConfigMapper;
    
    @Resource
    private ElectricityPayParamsService electricityPayParamsService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private ProfitSharingReceiverConfigService profitSharingReceiverConfigService;
    
    @Resource
    private ProfitSharingConfigTxService profitSharingConfigTxService;
    
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    /**
     * 初始化默认订单类型
     */
    static {
        
        List<Integer> orderTypes = Arrays.asList(ProfitSharingConfigOrderTypeEnum.BATTERY_PACKAGE.getCode(), ProfitSharingConfigOrderTypeEnum.INSURANCE.getCode(),
                ProfitSharingConfigOrderTypeEnum.BATTERY_SERVICE_FEE.getCode());
        Integer value = 0;
        for (int type : orderTypes) {
            value |= type;
        }
        
        DEFAULT_ORDER_TYPE = value;
    }
    
    public static final Integer DEFAULT_ORDER_TYPE;
    
    
    @Override
    public ProfitSharingConfigVO queryByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) {
        // 支付主配置查询
        ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(payParams)) {
            return null;
        }
        
        //查询分账主配置
        ProfitSharingConfig profitSharingConfig = this.queryByPayParamsIdFromCache(tenantId, payParams.getId());
        if (Objects.isNull(profitSharingConfig)) {
            return null;
        }
        
        ProfitSharingConfigVO profitSharingConfigVO = ProfitSharingConfigConverter.qryEntityToVo(profitSharingConfig);
        profitSharingConfigVO.setWechatMerchantId(payParams.getWechatMerchantId());
        profitSharingConfigVO.setConfigType(payParams.getConfigType());
        
        return profitSharingConfigVO;
    }
    
    @Override
    public List<ProfitSharingConfigVO> queryListByTenantIdAndFranchiseeIds(Integer tenantId, List<Long> franchiseeIds) {
        // 支付主配置查询
        List<ElectricityPayParams> electricityPayParams = electricityPayParamsService.queryListPreciseCacheByTenantIdAndFranchiseeId(tenantId, new HashSet<>(franchiseeIds));
        if (CollectionUtils.isEmpty(electricityPayParams)) {
            return null;
        }
        List<Integer> payParamIds = electricityPayParams.stream().map(ElectricityPayParams::getId).collect(Collectors.toList());
        
        List<ProfitSharingConfig> sharingConfigs = this.queryListByPayParamsIdsFromCache(tenantId, payParamIds);
        
        List<ProfitSharingConfigVO> profitSharingConfigVOS = ProfitSharingConfigConverter.qryEntityToVo(sharingConfigs);
        if (CollectionUtils.isEmpty(profitSharingConfigVOS)) {
            return null;
        }
        Map<Integer, ElectricityPayParams> payParamsMap = electricityPayParams.stream().collect(Collectors.toMap(ElectricityPayParams::getId, Function.identity()));
        profitSharingConfigVOS.stream().filter(profitSharingConfigVO -> payParamsMap.containsKey(profitSharingConfigVO.getPayParamId())).forEach(profitSharingConfigVO -> {
            ElectricityPayParams payParams = payParamsMap.get(profitSharingConfigVO.getPayParamId());
            profitSharingConfigVO.setWechatMerchantId(payParams.getWechatMerchantId());
            profitSharingConfigVO.setConfigType(payParams.getConfigType());
        });
        return profitSharingConfigVOS;
    }
    
    @Override
    public ProfitSharingConfig queryByPayParamsIdFromCache(Integer tenantId, Integer payParamsId) {
        String key = buildCacheKey(tenantId, payParamsId);
        String value = redisService.get(key);
        if (StringUtils.isNotBlank(value)) {
            ProfitSharingConfig profitSharingConfig = JsonUtil.fromJson(value, ProfitSharingConfig.class);
            return profitSharingConfig;
        }
        
        ProfitSharingConfig profitSharingConfig = profitSharingConfigMapper.selectByPayParamsIdAndTenantId(payParamsId, tenantId);
        if (Objects.isNull(profitSharingConfig)) {
            return null;
        }
        redisService.set(key, JsonUtil.toJson(profitSharingConfig));
        return profitSharingConfig;
    }
    
    
    @Override
    public List<ProfitSharingConfig> queryListByPayParamsIdsFromCache(Integer tenantId, List<Integer> payParamsIds) {
        List<String> cacheKeys = payParamsIds.stream().map(payParamsId -> buildCacheKey(tenantId, payParamsId)).collect(Collectors.toList());
        List<ProfitSharingConfig> sharingConfigs = redisService.multiJsonGet(cacheKeys, ProfitSharingConfig.class);
        
        Map<Integer, ProfitSharingConfig> existCacheMap = Optional.ofNullable(sharingConfigs).orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(ProfitSharingConfig::getPayParamId, Function.identity(), (k1, k2) -> k1));
        
        List<ProfitSharingConfig> resultList = new ArrayList<>();
        List<Integer> needQueryPayParamsIds = new ArrayList<>();
        
        payParamsIds.forEach(payParamsId -> {
            ProfitSharingConfig sharingConfig = existCacheMap.get(payParamsId);
            if (Objects.isNull(sharingConfig)) {
                needQueryPayParamsIds.add(payParamsId);
            } else {
                resultList.add(sharingConfig);
            }
        });
        
        if (CollectionUtils.isEmpty(needQueryPayParamsIds)) {
            return resultList;
        }
        
        List<ProfitSharingConfig> dbList = profitSharingConfigMapper.selectListByPayParamsIdsAndTenantId(tenantId, needQueryPayParamsIds);
        if (CollectionUtils.isEmpty(dbList)) {
            return resultList;
        }
        
        Map<String, String> cacheSaveMap = Maps.newHashMap();
        dbList.forEach(sharingConfig -> {
            cacheSaveMap.put(buildCacheKey(tenantId, sharingConfig.getPayParamId()), JsonUtil.toJson(sharingConfig));
            resultList.add(sharingConfig);
        });
        
        redisService.multiSet(cacheSaveMap);
        
        return resultList;
    }
    
    
    @Override
    public void updateStatus(ProfitSharingConfigUpdateStatusOptRequest request) {
        // 幂等校验
        this.checkIdempotent(request.getFranchiseeId());
        
        ElectricityPayParams payParams = electricityPayParamsService.queryPreciseCacheByTenantIdAndFranchiseeId(request.getTenantId(), request.getFranchiseeId());
        if (Objects.isNull(payParams)) {
            throw new BizException("请先添加小程序支付配置");
        }
        
        ProfitSharingConfig profitSharingConfig = this.queryByPayParamsIdFromCache(request.getTenantId(), payParams.getId());
        
        if (Objects.isNull(profitSharingConfig)) {
            // 初始化配置
            this.initProfitSharingConfig(request, payParams);
            return;
        }
        
        profitSharingConfigMapper.updateConfigStatusById(profitSharingConfig.getId(), request.getConfigStatus(), System.currentTimeMillis());
        
        // 清空缓存
        this.deleteCache(request.getTenantId(), profitSharingConfig.getPayParamId());
        
        operateStatueRecord(request.getFranchiseeId(), request.getConfigStatus());
        
    }
    
    
    @Override
    public void deleteCache(Integer tenantId, Integer payParamsId) {
        redisService.delete(buildCacheKey(tenantId, payParamsId));
    }
    
    @Override
    public void update(ProfitSharingConfigOptRequest request) {
        
        ProfitSharingConfig exist = profitSharingConfigMapper.selectByTenantIdAndId(request.getTenantId(), request.getId());
        if (Objects.isNull(exist)) {
            throw new BizException("数据不存在");
        }
        // 校验分账限制
        this.checkScaleLimit(request, exist);
        
        //更新
        ProfitSharingConfig profitSharingConfig = ProfitSharingConfigConverter.optRequestToEntity(request);
        profitSharingConfig.setUpdateTime(System.currentTimeMillis());
        profitSharingConfigMapper.update(profitSharingConfig);
        // 清空缓存
        this.deleteCache(request.getTenantId(), exist.getPayParamId());
        
        this.operateRecord(exist, profitSharingConfig);
    }
    
    @Override
    public void removeByPayParamId(Integer tenantId, Integer payParamsId) {
        ProfitSharingConfig profitSharingConfig = queryByPayParamsIdFromCache(tenantId, payParamsId);
        if (Objects.isNull(profitSharingConfig)) {
            return;
        }
        Long removeId = profitSharingConfig.getId();
        
        List<ProfitSharingReceiverConfig> receiverConfigs = profitSharingReceiverConfigService.queryListByProfitSharingConfigId(tenantId, removeId);
        List<Long> receiverIds = null;
        if (CollectionUtils.isNotEmpty(receiverConfigs)) {
            receiverIds = receiverConfigs.stream().map(ProfitSharingReceiverConfig::getId).collect(Collectors.toList());
        }
        profitSharingConfigTxService.remove(tenantId, removeId, receiverIds);
        
        this.deleteCache(tenantId, profitSharingConfig.getPayParamId());
    }
    
    @Slave
    @Override
    public ProfitSharingConfig queryById(Integer tenantId, Long id) {
        return profitSharingConfigMapper.selectByTenantIdAndId(tenantId, id);
    }
    
    /**
     * 校验分账比例限制总金额
     *
     * @param request
     * @param exist
     * @author caobotao.cbt
     * @date 2024/8/23 15:01
     */
    private void checkScaleLimit(ProfitSharingConfigOptRequest request, ProfitSharingConfig exist) {
        
        BigDecimal scaleLimit = request.getScaleLimit();
        
        if (Objects.isNull(scaleLimit) || scaleLimit.compareTo(exist.getScaleLimit()) >= 0) {
            return;
        }
        
        
        if (!ProfitSharingConfigProfitSharingTypeEnum.ORDER_SCALE.getCode().equals(exist.getProfitSharingType())) {
            return;
        }
    
        // 分账比例小于原比例
        
        List<ProfitSharingReceiverConfig> configs = profitSharingReceiverConfigService.queryListByProfitSharingConfigId(exist.getTenantId(), exist.getId());
        
        // 计算累计总比例
        BigDecimal sum = BigDecimal.ZERO;
        sum = sum.add(scaleLimit);
        if (CollectionUtils.isNotEmpty(configs)) {
            for (ProfitSharingReceiverConfig config : configs) {
                sum = sum.add(config.getScale());
            }
        }
        
        if (sum.compareTo(request.getScaleLimit()) > 0) {
            throw new BizException("分账接收方分账比例之和 必须小于等于 允许比例上限");
        }
        
    }
    
    
    /**
     * 构建缓存key
     */
    private String buildCacheKey(Integer tenantId, Integer payParamsId) {
        return String.format(CacheConstant.PROFIT_SHARING_PAY_PARAMS_ID_KEY, tenantId, payParamsId);
    }
    
    /**
     * 初始化分账方配置
     *
     * @param request
     * @param payParams
     * @author caobotao.cbt
     * @date 2024/8/23 08:51
     */
    private ProfitSharingConfig initProfitSharingConfig(ProfitSharingConfigUpdateStatusOptRequest request, ElectricityPayParams payParams) {
        ProfitSharingConfig profitSharingConfig = profitSharingConfigMapper.selectByTenantIdAndFranchiseeId(request.getTenantId(), request.getFranchiseeId());
        if (Objects.nonNull(profitSharingConfig)) {
            log.error("ProfitSharingConfigServiceImpl.initProfitSharingConfig WARN! param error id:{},franchiseeId:{},config exist!", profitSharingConfig.getId(),
                    profitSharingConfig.getFranchiseeId());
            throw new BizException("加盟商:" + request.getFranchiseeId() + "配置已存在");
        }
        ProfitSharingConfig insert = this.buildInitProfitSharingConfig(request, payParams);
        profitSharingConfigMapper.insert(insert);
        return insert;
    }
    
    /**
     * 初始化分账方配置
     *
     * @param request
     * @param payParams
     * @author caobotao.cbt
     * @date 2024/8/23 09:10
     */
    private ProfitSharingConfig buildInitProfitSharingConfig(ProfitSharingConfigUpdateStatusOptRequest request, ElectricityPayParams payParams) {
        ProfitSharingConfig profitSharingConfig = new ProfitSharingConfig();
        
        profitSharingConfig.setTenantId(request.getTenantId());
        profitSharingConfig.setFranchiseeId(request.getFranchiseeId());
        profitSharingConfig.setPayParamId(payParams.getId());
        profitSharingConfig.setConfigStatus(request.getConfigStatus());
        profitSharingConfig.setOrderType(DEFAULT_ORDER_TYPE);
        profitSharingConfig.setAmountLimit(BigDecimal.ZERO);
        profitSharingConfig.setProfitSharingType(ProfitSharingConfigProfitSharingTypeEnum.ORDER_SCALE.getCode());
        profitSharingConfig.setScaleLimit(BigDecimal.ZERO);
        profitSharingConfig.setCycleType(ProfitSharingConfigCycleTypeEnum.D_1.getCode());
        profitSharingConfig.setCreateTime(System.currentTimeMillis());
        profitSharingConfig.setUpdateTime(System.currentTimeMillis());
        return profitSharingConfig;
    }
    
    /**
     * 幂等校验
     *
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/23 08:54
     */
    private void checkIdempotent(Long franchiseeId) {
        boolean b = redisService.setNx(String.format(CacheConstant.PROFIT_SHARING_IDEMPOTENT_KEY, franchiseeId), "1", 3000L, true);
        if (!b) {
            throw new BizException("频繁操作");
        }
    }
    
    
    private void operateRecord(ProfitSharingConfig old, ProfitSharingConfig newConfig) {
        
        Long franchiseeId = newConfig.getFranchiseeId();
        
        String franchiseeName = MultiFranchiseeConstant.DEFAULT_FRANCHISEE_NAME;
        if (!MultiFranchiseeConstant.DEFAULT_FRANCHISEE.equals(franchiseeId)) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            franchiseeName = Optional.ofNullable(franchisee).orElse(new Franchisee()).getName();
        }
        String oldScaleLimit = old.getScaleLimit().multiply(new BigDecimal(100)).stripTrailingZeros() + "%";
        String newScaleLimit = newConfig.getScaleLimit().multiply(new BigDecimal(100)).stripTrailingZeros() + "%";
        
        String oldAmountLimit = old.getAmountLimit().toString();
        String newAmountLimit = newConfig.getAmountLimit().toString();
        
        
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("franchiseeName", franchiseeName);
        record.put("scaleLimit", newScaleLimit);
        record.put("amountLimit", newAmountLimit);
        
        Map<String, String> oldRecord = Maps.newHashMapWithExpectedSize(1);
        oldRecord.put("scaleLimit", oldScaleLimit);
        oldRecord.put("amountLimit", oldAmountLimit);
        operateRecordUtil.record(oldRecord, record);
    }
    
    private void operateStatueRecord(Long franchiseeId, Integer configStatus) {
        
        String franchiseeName = MultiFranchiseeConstant.DEFAULT_FRANCHISEE_NAME;
        if (!MultiFranchiseeConstant.DEFAULT_FRANCHISEE.equals(franchiseeId)) {
            Franchisee franchisee = franchiseeService.queryByIdFromCache(franchiseeId);
            franchiseeName = Optional.ofNullable(franchisee).orElse(new Franchisee()).getName();
        }
        
        Map<String, String> record = Maps.newHashMapWithExpectedSize(1);
        record.put("franchiseeName", franchiseeName);
        record.put("statusDesc", ProfitSharingConfigStatusEnum.MAP.get(configStatus).getDesc());
        operateRecordUtil.record(null, record);
    }
}
