/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/27
 */

package com.xiliulou.electricity.enums.notify;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * description: 设备状态枚举；
 *
 * @author caobotao.cbt
 * @date 2024/6/27 17:14
 */
@Getter
public enum DeviceStatusEnum {
    
    DEVICE_ONLINE("online", "上线", "-"),
    DEVICE_OFFLINE("offline", "下线", "请及时在运维端查看设备状态，长时间离线，请检查网络和流量，您任何疑问请联系西六楼客服");
    
    
    DeviceStatusEnum(String status, String statusMsg, String remarkMsg) {
        this.status = status;
        this.statusMsg = statusMsg;
        this.remarkMsg = remarkMsg;
    }
    
    private static final Map<String, DeviceStatusEnum> map = new HashMap<>();
    
    static {
        for (DeviceStatusEnum value : values()) {
            map.put(value.status, value);
        }
    }
    
    private String status;
    
    private String statusMsg;
    
    private String remarkMsg;
    
    /**
     * 根据status获取枚举
     *
     * @param status
     * @author caobotao.cbt
     * @date 2024/6/26 19:24
     */
    public static Optional<DeviceStatusEnum> getByStatus(String status) {
        DeviceStatusEnum deviceStatusEnum = map.get(status);
        return Optional.ofNullable(deviceStatusEnum);
    }
}
