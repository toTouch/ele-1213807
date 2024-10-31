package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryType;

import java.util.List;

/**
 * (MemberCardBatteryType)表服务接口
 *
 * @author zzlong
 * @since 2023-07-07 14:07:42
 */
public interface MemberCardBatteryTypeService {

    Integer batchInsert(List<MemberCardBatteryType> buildMemberCardBatteryTypeList);

    List<String> selectBatteryTypeByMid(Long id);
    
    List<String> checkBatteryTypeWithMemberCard(Long uid, String batteryType, BatteryMemberCard memberCard);
}
