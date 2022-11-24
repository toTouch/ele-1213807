package com.xiliulou.electricity.entity;

import lombok.Data;

/**
 * 电池电压电流、充电器电压电流变化
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-11-23-10:47
 */
@Data
public class VoltageCurrentChange {

    private Long electricityCabinetId;
    private Integer cellNo;
    //充电器电压
    private Double chargeV;
    //充电器电流
    private Double chargeA;
    //电池电压
    private Double batteryChargeV;
    //电池电流
    private Double batteryChargeA;

    private String sessionId;
    private Long reportTime;
    private Long createTime;
}
