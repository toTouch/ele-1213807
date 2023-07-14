package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.UserBatteryType;

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
}
