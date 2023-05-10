package com.xiliulou.electricity.dto.bms;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/5/9 13:20
 */
@Data
public class BatteryInfoDto {
    /**
     * 电池编号
     */
    private String sn;
    /**
     * 电池电压
     */
    private Double batteryV;
    /**
     * 电池电流
     */
    private Double batteryA;
    /**
     * soc
     */
    private Integer soc;
    /**
     * 电池容量
     */
    private Double capacity;
    /**
     * 电池状态
     */
    private Integer batteryStatus;
    /**
     * 电芯的个数
     */
    private Integer coreNum;
    /**
     * 电池内部PCB板表面温度
     */
    private Double hardwareTemp;
    /**
     * 电池内部多组电芯中间表面温度
     */
    private Double coreTemp;
    /**
     * 环境温度
     */
    private Double envTemp;
    /**
     * 电池控制
     */
    private Integer controlStatus;
    /**
     * 均衡标志位
     */
    private Integer equalsStatus;
    /**
     * 总放电Ah
     */
    private Integer totalDischarge;
    /**
     * 总充电Ah
     */
    private Integer totalCharge;
    /**
     * 预计放电时间
     */
    private Integer expectDischargeTime;
    /**
     * 总放电次数
     */
    private Integer circleNum;
    /**
     * 设备重置
     */
    private Integer deviceReset;
    /**
     * BMS开关设置
     */
    private Integer bmsSwitch;
    /**
     * 速度
     */
    private Double speed;
    /**
     * 行驶里程
     */
    private Double driverDistance;
    /**
     * GSM小区信息
     */
    private String gsmType;
    /**
     * GSM信号强度
     */
    private Integer gsmSignalStrength;
    /**
     * imsi信息
     */
    private String imsi;

    private Long createTime;

    private Long updateTime;

    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;

    /**
     * 0--在线 1--不在线
     */
    private Integer onlineStatus;
}
