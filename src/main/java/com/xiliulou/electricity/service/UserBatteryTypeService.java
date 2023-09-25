package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.entity.UserInfo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (UserBatteryType)表服务接口
 *
 * @author zzlong
 * @since 2023-07-14 16:02:42
 */
public interface UserBatteryTypeService {

    UserBatteryType queryByIdFromDB(Long id);

    UserBatteryType queryByIdFromCache(Long id);

    Integer insert(UserBatteryType userBatteryType);

    Integer update(UserBatteryType userBatteryType);

    Boolean deleteById(Long id);

    Integer batchInsert(List<UserBatteryType> buildUserBatteryType);

    List<UserBatteryType> buildUserBatteryType(List<String> batteryTypeList, UserInfo userInfo);

    Integer deleteByUid(Long uid);

    List<String> selectByUid(Long uid);

    String selectUserMaxBatteryType(Long uid);

    String selectUserSimpleBatteryType(Long uid);

    void updateUserBatteryType(ElectricityMemberCardOrder electricityMemberCardOrder,UserInfo userInfo);

    void synchronizedUserBatteryType(Long uid, Integer tenantId, List<String> batteryTypes);

    String selectOneByUid(Long uid);

    Triple<Boolean, String, Object> selectUserBatteryTypeByUid(Long uid);

    Triple<Boolean, String, Object> modifyUserBatteryType(UserBatteryType userBatteryType);
}
