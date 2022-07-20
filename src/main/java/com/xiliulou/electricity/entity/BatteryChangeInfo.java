package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * 柜机电池变化记录
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-07-07-11:21
 */
@Data
public class BatteryChangeInfo {

    private String electricityCabinetId;
    private Integer cellNo;
    private String sessionId;
    private String preBatteryName;
    private String changeBatteryName;
    private Long reportTime;
    private Long createTime;

}
