package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 柜机异常发送公众号通知
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-10-17-19:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectricityAbnormalMessageNotify {
    
    /**
     * 网点名称
     */
    private String address;
    /**
     * 设备编号
     */
    private String equipmentNumber;
    /**
     * 异常类型
     */
    private Long exceptionType;
    /**
     * 异常描述
     */
    private String description;
    /**
     * 发生时间
     */
    private String reportTime;
    
    //异常类型  00000:电池满仓 ，80004：烟雾告警 2:后门异常打开
    public static final Long BATTERY_FULL_TYPE=00000L;
    public static final Long SMOKE_WARN_ERROR_CODE = 80004L;
    public static final Long BACK_DOOR_OPEN_TYPE=2L;
    
}
