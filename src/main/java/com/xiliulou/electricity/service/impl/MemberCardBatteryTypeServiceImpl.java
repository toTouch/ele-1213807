package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.enums.FlexibleRenewalEnum;
import com.xiliulou.electricity.mapper.MemberCardBatteryTypeMapper;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

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
    public List<String> checkBatteryTypeWithMemberCard(Long uid, String batteryType, List<String> userBatteryTypes) {
        // 用户当前套餐不分型号，不作处理
        if (CollectionUtils.isEmpty(userBatteryTypes)) {
            return List.of();
        }
        
        // 用户当前绑定的电池也就是要还的电池和当前套餐匹配，不作处理
        if (userBatteryTypes.contains(batteryType)) {
            return List.of();
        }
        
        // 不匹配时，从缓存内获取旧套餐的电池型号
        return redisService.getWithList(String.format(CacheConstant.BATTERY_MEMBER_CARD_TRANSFORM, uid), String.class);
    }
    
    @Override
    public boolean checkBatteryTypeWithUser(List<String> userBatteryTypes, BatteryMemberCard memberCard, ElectricityConfig electricityConfig) {
        List<String> memberCardBatteryTypes = selectBatteryTypeByMid(memberCard.getId());
        
        if (CollectionUtils.isEmpty(userBatteryTypes) && CollectionUtils.isEmpty(memberCardBatteryTypes)) {
            return true;
        }
        
        if (CollectionUtils.isEmpty(userBatteryTypes) || CollectionUtils.isEmpty(memberCardBatteryTypes)) {
            return false;
        }
        
        return !Objects.equals(electricityConfig.getIsEnableFlexibleRenewal(), FlexibleRenewalEnum.NORMAL.getCode()) || CollectionUtils.containsAll(memberCardBatteryTypes,
                userBatteryTypes);
    }
}
