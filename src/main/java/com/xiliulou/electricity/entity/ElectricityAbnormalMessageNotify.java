package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 柜机异常发送公众号通知
 *
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
    private Integer exceptionType;
    /**
     * 异常描述
     */
    private String description;
    /**
     * 发生时间
     */
    private String reportTime;

}
