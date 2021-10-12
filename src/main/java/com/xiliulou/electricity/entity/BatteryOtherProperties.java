package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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

}
