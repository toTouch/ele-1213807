package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Map;

/**
 * (UserBatteryType)表服务接口
 *
 * @author zzlong
 * @since 2023-07-14 16:02:42
 */
public interface UserBatteryTypeService {

    Integer batchInsert(List<UserBatteryType> buildUserBatteryType);

    List<UserBatteryType> buildUserBatteryType(List<String> batteryTypeList, UserInfo userInfo);

    Integer deleteByUid(Long uid);

    List<String> selectByUid(Long uid);

    String selectUserSimpleBatteryType(Long uid);

    void updateUserBatteryType(ElectricityMemberCardOrder electricityMemberCardOrder,UserInfo userInfo);

    void synchronizedUserBatteryType(Long uid, Integer tenantId, List<String> batteryTypes);

    String selectOneByUid(Long uid);

    Triple<Boolean, String, Object> selectUserBatteryTypeByUid(Long uid);

    Triple<Boolean, String, Object> modifyUserBatteryType(UserBatteryType userBatteryType);
    
    List<UserBatteryType> listByUid(Long uid);
    
    Integer deleteByUidAndBatteryTypes(Long uid, List<String> batteryTypes);
    
    /**
     * 根据uidList查询其电池型号对应的短型号
     */
    Map<Long, List<String>> listShortBatteryByUidList(List<Long> uidList, Integer tenantId);
}
