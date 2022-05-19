package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/4/12 10:56
 */
@Data
public class MqDeviceNotify {
    /**
     * 产品号
     */
    private String productKey;
    /**
     * 设备号
     */
    private String deviceSn;
    /**
     * 设备上下线发生的时间
     */
    private String occurTime;
    /**
     * 设备状态
     */
    private String status;
    /**
     * 项目名
     */
    private String projectName;
    /**
     * 设备名称
     */
    private String deviceName;
}
