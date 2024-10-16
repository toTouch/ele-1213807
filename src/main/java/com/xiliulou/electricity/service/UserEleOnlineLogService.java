package com.xiliulou.electricity.service;

import org.apache.commons.lang3.tuple.Pair;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserEleOnlineLog;

public interface UserEleOnlineLogService {
    
    
    /**
     * 插入新的用户电力在线日志
     * @param userEleOnlineLog 用户电力在线日志实体
     * @return 插入的记录数
     */
    int insert(UserEleOnlineLog userEleOnlineLog);

    /**
     * 生成用户设备状态值
     * @param onlineStatus 在线状态
     * @param occurTime 发生时间
     * @return 生成的状态值字符串
     */
    String generateUserDeviceStatusValue(Integer onlineStatus, Long occurTime);

    /**
     * 解析用户设备状态值
     * @param value 状态值字符串
     * @return 包含在线状态和发生时间的键值对
     */
    Pair<Integer, Long> parseUserDeviceStatusValue(String value);

    UserEleOnlineLog queryLastLog(Integer id);


    R queryOnlineLogList(Integer size, Integer offset, String type, Integer eleId);

    R queryOnlineLogCount(String type, Integer eleId);
}