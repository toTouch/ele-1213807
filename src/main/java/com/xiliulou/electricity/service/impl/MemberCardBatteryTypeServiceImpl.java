package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryDeposit;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.mapper.MemberCardBatteryTypeMapper;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

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
    public List<String> getBatteryTypesForCheck(Long uid, String batteryType, List<String> userBatteryTypes) {
        // 用户当前套餐不分型号，不作处理
        if (CollectionUtils.isEmpty(userBatteryTypes)) {
            return List.of();
        }
        
        // 用户当前绑定的电池也就是要还的电池和当前套餐匹配，不作处理
        if (userBatteryTypes.contains(batteryType)) {
            return List.of();
        }
        
        // 不匹配时，从缓存内获取旧套餐的电池型号
        String listStr = redisService.get(String.format(CacheConstant.BATTERY_MEMBER_CARD_TRANSFORM, uid));
        if (StringUtils.isBlank(listStr) || StringUtils.isEmpty(listStr)) {
            return List.of();
        }
        
        return JsonUtil.fromJsonArray(listStr, String.class);
    }
    
    @Override
    public boolean checkBatteryTypeAndDepositWithUser(List<String> userBatteryTypes, BatteryMemberCard memberCard, UserBatteryDeposit userBatteryDeposit,
            ElectricityConfig electricityConfig) {
        // 灵活续费押金校验
        if (Objects.nonNull(userBatteryDeposit) && Objects.equals(electricityConfig.getIsEnableFlexibleRenewal(), FlexibleRenewalEnum.NORMAL.getCode()) && !Objects.equals(
                userBatteryDeposit.getBatteryDeposit(), memberCard.getDeposit())) {
            // TODO SJP
            log.info("调试，押金错误");
            return false;
        }
        
        List<String> memberCardBatteryTypes = selectBatteryTypeByMid(memberCard.getId());
        
        // 单型号套餐续费
        if (CollectionUtils.isEmpty(userBatteryTypes) && CollectionUtils.isEmpty(memberCardBatteryTypes)) {
            return true;
        }
        
        // 单型号套餐续费已被排除，非灵活续费场景下，只要有一个为单型号，就不匹配
        if (Objects.equals(electricityConfig.getIsEnableFlexibleRenewal(), FlexibleRenewalEnum.NORMAL.getCode())) {
            if ((CollectionUtils.isEmpty(userBatteryTypes) || CollectionUtils.isEmpty(memberCardBatteryTypes))) {
                return false;
            }
        }
        
        // 灵活续费时，不校验型号，非灵活续费，套餐型号应当包含用户绑定的型号
        return !Objects.equals(electricityConfig.getIsEnableFlexibleRenewal(), FlexibleRenewalEnum.NORMAL.getCode()) || CollectionUtils.containsAll(memberCardBatteryTypes,
                userBatteryTypes);
    }
}
