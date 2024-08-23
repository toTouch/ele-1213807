

package com.xiliulou.electricity.service.impl.profitsharing;

import java.math.BigDecimal;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.converter.profitsharing.ProfitSharingConfigConverter;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigCycleTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigOrderTypeEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigProfitSharingTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingConfigMapper;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingConfigUpdateStatusOptRequest;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        
        Integer profitSharingType = request.getProfitSharingType();
        BigDecimal scaleLimit = request.getScaleLimit();
        
        if (Objects.nonNull(scaleLimit) && scaleLimit.compareTo(exist.getScaleLimit()) < 0) {
            // 分账比例小于原比例
            if (Objects.isNull(profitSharingType)) {
                profitSharingType = exist.getProfitSharingType();
            }
            
            if (ProfitSharingConfigProfitSharingTypeEnum.ORDER_SCALE.getCode().equals(exist.getProfitSharingType())) {
            
            }
        }
        
        ProfitSharingConfig profitSharingConfig = ProfitSharingConfigConverter.optRequestToEntity(request);
        profitSharingConfig.setUpdateTime(System.currentTimeMillis());
        profitSharingConfigMapper.update(profitSharingConfig);
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
        if (Objects.isNull(profitSharingConfig)) {
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
}
