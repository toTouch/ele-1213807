package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/4/12 17:37
 */
@Data
public class MqHardwareNotify {
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 发生的时间
     */
    private String occurTime;
    /**
     * 错误消息
     */
    private String errMsg;
    /**
     * 消息标题
     */
    private String projectTitle;

    public static final String LOCK_CELL_PROJECT_TITLE = "餐柜格挡异常";
}
