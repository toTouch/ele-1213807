package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
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
        
        if (Objects.equals(isEnableFlexibleRenewal, FlexibleRenewalEnum.EXCHANGE_BATTERY.getCode()) || Objects.equals(isEnableFlexibleRenewal,
                FlexibleRenewalEnum.RETURN_BEFORE_RENT.getCode())) {
            
            // 必须修改为灵活续费之后才去判断大小，避免isEnableFlexibleRenewal为null时逻辑错误
            if (Objects.isNull(userBatteryDeposit) || Objects.isNull(userBatteryDeposit.getBatteryDeposit())
                    || memberCard.getDeposit().compareTo(userBatteryDeposit.getBatteryDeposit()) > 0) {
                log.info("FLEXIBLE RENEWAL INFO! normal renewal deposit do not match! uid={}", userInfo.getUid());
                return false;
            }
        } else {
            // 灵活续费关闭状态
            if (!Objects.equals(userBatteryDeposit.getBatteryDeposit(), memberCard.getDeposit())) {
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
}
