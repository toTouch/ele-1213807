package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.MemberCardBatteryType;

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
    
    List<String> checkBatteryTypeWithMemberCard(Long uid, String batteryType, List<String> userBatteryTypes);
    
    /**
     * 检查用户绑定的电池型号、套餐绑定的电池型号、租户配置是否相符，true为符合，false为不符合
     *
     * @param userBatteryTypeList 用户绑定的电池型号
     * @param memberCard          套餐
     * @param electricityConfig   租户配置
     * @return 检查结果
     */
    boolean checkBatteryTypeWithUser(List<String> userBatteryTypeList, BatteryMemberCard memberCard, ElectricityConfig electricityConfig);
}
