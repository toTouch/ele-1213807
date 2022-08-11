package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/4/12 10:56
 */
@Data
public class MqMalfunctionNotify {

    /**
     * 消息标题
     */
    private String projectTitle;

    /**
     * 设备故障上报时间
     */
    private String occurTime;

    /**
     * 错误消息
     */
    private String errMsg;

    /**
     * 设备名称
     */
    private String deviceName;
}
