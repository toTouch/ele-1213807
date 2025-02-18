package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.mapper.MemberCardBatteryTypeMapper;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * (MemberCardBatteryType)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-07 14:07:42
 */
@Service("memberCardBatteryTypeService")
@Slf4j
public class MemberCardBatteryTypeServiceImpl implements MemberCardBatteryTypeService {
    
    @Resource
    private MemberCardBatteryTypeMapper memberCardBatteryTypeMapper;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Qualifier("redisService")
    @Autowired
    private RedisService redisService;
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<MemberCardBatteryType> memberCardBatteryTypeList) {
        return this.memberCardBatteryTypeMapper.batchInsert(memberCardBatteryTypeList);
    }
    
    @Override
    public List<String> selectBatteryTypeByMid(Long mid) {
        return this.memberCardBatteryTypeMapper.selectBatteryTypeByMid(mid);
    }
    
    @Override
    public boolean checkBatteryTypeAndDepositWithUser(List<String> userBatteryTypes, BatteryMemberCard memberCard, UserBatteryDeposit userBatteryDeposit,
            ElectricityConfig electricityConfig, UserInfo userInfo) {
        // 灵活续费押金校验
        Integer isEnableFlexibleRenewal = electricityConfig.getIsEnableFlexibleRenewal();
        BigDecimal deposit =
                (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES) || Objects.equals(userBatteryDeposit.getDepositModifyFlag(),
                        UserBatteryDeposit.DEPOSIT_MODIFY_SPECIAL)) ? userBatteryDeposit.getBeforeModifyDeposit() : userBatteryDeposit.getBatteryDeposit();
        
        if (Objects.equals(isEnableFlexibleRenewal, FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode()) || Objects.equals(isEnableFlexibleRenewal,
                FlexibleRenewalEnum.RETURN_BEFORE_RENT.getCode())) {
            
            // 必须修改为灵活续费之后才去判断大小，避免isEnableFlexibleRenewal为null时逻辑错误
            if (Objects.isNull(deposit) || memberCard.getDeposit().compareTo(deposit) > 0) {
                log.info("FLEXIBLE RENEWAL INFO! normal renewal deposit do not match! uid={}", userInfo.getUid());
                return false;
            }
        } else {
            // 灵活续费关闭状态
            if (!Objects.equals(deposit, memberCard.getDeposit())) {
                log.info("FLEXIBLE RENEWAL INFO! flexible renewal deposit do not match! uid={}", userInfo.getUid());
                return false;
            }
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(memberCard.getFranchiseeId());
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            log.info("FLEXIBLE RENEWAL INFO! It is not renewal! uid={}", userInfo.getUid());
            return true;
        }
        
        List<String> memberCardBatteryTypes = selectBatteryTypeByMid(memberCard.getId());
        
        // 多型号加盟商。免押或单独交押金后初次购买时
        if (CollectionUtils.isEmpty(userBatteryTypes)) {
            log.info("FLEXIBLE RENEWAL INFO! new model type franchisee first time pay! uid={}, electricityConfig={}", userInfo.getUid(), electricityConfig);
            return true;
        }
        
        if (Objects.equals(isEnableFlexibleRenewal, FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode()) || Objects.equals(isEnableFlexibleRenewal,
                FlexibleRenewalEnum.RETURN_BEFORE_RENT.getCode())) {
            log.info("FLEXIBLE RENEWAL INFO! Flexible renewal is open, do not check battery types! uid={}, electricityConfig={}", userInfo.getUid(), electricityConfig);
            return true;
        }
        
        // 灵活续费时，不校验型号，非灵活续费，套餐型号应当包含用户绑定的型号
        if (CollectionUtils.isEmpty(memberCardBatteryTypes) || !CollectionUtils.containsAll(memberCardBatteryTypes, userBatteryTypes)) {
            log.info("FLEXIBLE RENEWAL INFO! Battery types do not contains! uid={}, electricityConfig={}", userInfo.getUid(), electricityConfig);
            return false;
        }
        
        return true;
    }
    
    /**
     * 校验用户绑定的电池型号和要购买的套餐是否匹配
     */
    @Override
    public Boolean checkBatteryTypesForRenew(List<String> userBatteryTypes, BatteryMemberCard memberCard, UserBatteryDeposit userBatteryDeposit, Franchisee franchisee,
            UserInfo userInfo) {
        // 押金必须相等，新旧型号加盟商都必须校验押金
        BigDecimal deposit =
                (Objects.equals(userBatteryDeposit.getDepositModifyFlag(), UserBatteryDeposit.DEPOSIT_MODIFY_YES) || Objects.equals(userBatteryDeposit.getDepositModifyFlag(),
                        UserBatteryDeposit.DEPOSIT_MODIFY_SPECIAL)) ? userBatteryDeposit.getBeforeModifyDeposit() : userBatteryDeposit.getBatteryDeposit();
        if (Objects.isNull(userBatteryDeposit.getBatteryDeposit()) || Objects.isNull(memberCard.getDeposit()) || deposit.compareTo(memberCard.getDeposit()) != 0) {
            log.info("BATTERY DEPOSIT INFO! deposit do not match. uid={}", userInfo.getUid());
            return Boolean.FALSE;
        }
        
        // 不分型号加盟商不校验灵活续费电池型号
        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            log.info("BATTERY DEPOSIT INFO! old model type franchisee. uid={}", userInfo.getUid());
            return Boolean.TRUE;
        }
        
        List<String> memberCardBatteryTypes = selectBatteryTypeByMid(memberCard.getId());
        
        // 单型号已过滤，用户绑定的型号为空为初次购买，加盟商隔离无法从单型号续费多型号
        if (CollectionUtils.isEmpty(userBatteryTypes)) {
            log.info("BATTERY DEPOSIT INFO! first time to buy. uid={}", userInfo.getUid());
            return Boolean.TRUE;
        } else {
            
            if (CollectionUtils.isEmpty(memberCardBatteryTypes)) {
                log.info("BATTERY DEPOSIT INFO! memberCardBatteryTypes is null. uid={}", userInfo.getUid());
                return Boolean.FALSE;
            } else {
                return CollectionUtils.containsAll(memberCardBatteryTypes, userBatteryTypes);
            }
        }
    }
    
    @Slave
    @Override
    public List<MemberCardBatteryType> listByMemberCardIds(Integer tenantId, List<Long> memberCardIds) {
        return memberCardBatteryTypeMapper.selectListByMemberCardIds(tenantId, memberCardIds);
    }
}
