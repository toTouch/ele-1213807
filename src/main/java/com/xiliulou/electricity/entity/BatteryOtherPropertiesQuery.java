package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatteryOtherPropertiesQuery {

    private Integer id;
    //电池的标称电压
    private Double batteryV;
    /**
     * 电芯的数量
     */
    private Integer batteryCoreNum;
    /**
     * 电池的剩余容量
     */
    private Double batteryRemainCapacity;
    /**
     * 电池健康状态
     */
    private Double soh;
    /**
     * 电池充电电流
     */
    private Double batteryChargeA;
    /**
     * 环境温度
     */
    private Double envTemp;
    /**
     * 电芯温度
     */
    private Double batteryCoreTemp;

    /**
     * 板卡温度
     */
    private Double hardwareTemp;
    /**
     * 电芯电压
     */
    private List batteryCoreVList = new ArrayList<>();
    /**
     * 电池健康状态，分为7部分，二进制
     */
    private String batteryHealthPartOne;

    private String batteryHealthPartTwo;

    private String batteryHealthPartThree;

    private String batteryHealthPartFour;

    private String batteryHealthPartFive;

    private String batteryHealthPartSix;

    private String batteryHealthPartSeven;

    private Long createTime;

    private Long updateTime;

    private String batteryName;

    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 	gsm小区信息 01104001
     */
    private String gsmType;

    /**
     * 	gsm信号强度 01106001
     */
    private Long gsmSignalStrength;
    /**
     * 	电池状态 01108001
     */
    private Long batteryStatus;
    /**
     * 	速度信息 01109001
     */
    private Long speed;
    /**
     * 	行驶里程 01110001
     */
    private Long tripMiles;
    /**
     * 	总电压 01111001
     */
    private Long sumV;
    /**
     * 	总电流 01112001
     */
    private Long sumA;

    /**
     * 	功率温度值  01118001
     */
    private Long powerTemp;

    /**
     * 	总放电 01124001
     */
    private Long sumDischarge;
    /**
     * 	总充电 01125001
     */
    private Long sumCharge;
    /**
     * 	预计放电时间 01126001
     */
    private Long expDischargeTime;


}
