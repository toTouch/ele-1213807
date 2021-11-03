package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@TableName("t_battery_other_properties")
public class BatteryOtherProperties {

    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    //电池的标称电压
    private double batteryV;
    /**
     * 电芯的数量
     */
    private int batteryCoreNum;
    /**
     * 电池的剩余容量
     */
    private double batteryRemainCapacity;
    /**
     * 电池健康状态
     */
    private double soh;
    /**
     * 电池充电电流
     */
    private double batteryChargeA;
    /**
     * 环境温度
     */
    private double envTemp;
    /**
     * 电芯温度
     */
    private double batteryCoreTemp;

    /**
     * 板卡温度
     */
    private double hardwareTemp;
    /**
     * 电芯电压
     */
    private List<Double> batteryCoreVList = new ArrayList<>();
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
    private Integer gsmSignalStrength;
    /**
     * 	电池状态 01108001
     */
    private Integer batteryStatus;
    /**
     * 	速度信息 01109001
     */
    private Integer speed;
    /**
     * 	行驶里程 01110001
     */
    private Integer tripMiles;
    /**
     * 	总电压 01111001
     */
    private Integer sumV;
    /**
     * 	总电流 01112001
     */
    private Integer sumA;

    /**
     * 	功率温度值  01118001
     */
    private Integer powerTemp;

    /**
     * 	总放电 01124001
     */
    private Integer sumDischarge;
    /**
     * 	总充电 01125001
     */
    private Integer sumCharge;
    /**
     * 	预计放电时间 01126001
     */
    private Integer expDischargeTime;


}
