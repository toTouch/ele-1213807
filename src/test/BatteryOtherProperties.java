package com.xiliulou.serialsdk.entity;

import java.util.ArrayList;
import java.util.List;

public class BatteryOtherProperties {
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


    public String getBatteryHealthPartOne() {
        return batteryHealthPartOne;
    }

    public void setBatteryHealthPartOne(String batteryHealthPartOne) {
        this.batteryHealthPartOne = batteryHealthPartOne;
    }

    public String getBatteryHealthPartTwo() {
        return batteryHealthPartTwo;
    }

    public void setBatteryHealthPartTwo(String batteryHealthPartTwo) {
        this.batteryHealthPartTwo = batteryHealthPartTwo;
    }

    public String getBatteryHealthPartThree() {
        return batteryHealthPartThree;
    }

    public void setBatteryHealthPartThree(String batteryHealthPartThree) {
        this.batteryHealthPartThree = batteryHealthPartThree;
    }

    public String getBatteryHealthPartFour() {
        return batteryHealthPartFour;
    }

    public void setBatteryHealthPartFour(String batteryHealthPartFour) {
        this.batteryHealthPartFour = batteryHealthPartFour;
    }

    public String getBatteryHealthPartFive() {
        return batteryHealthPartFive;
    }

    public void setBatteryHealthPartFive(String batteryHealthPartFive) {
        this.batteryHealthPartFive = batteryHealthPartFive;
    }

    public String getBatteryHealthPartSix() {
        return batteryHealthPartSix;
    }

    public void setBatteryHealthPartSix(String batteryHealthPartSix) {
        this.batteryHealthPartSix = batteryHealthPartSix;
    }

    public String getBatteryHealthPartSeven() {
        return batteryHealthPartSeven;
    }

    public void setBatteryHealthPartSeven(String batteryHealthPartSeven) {
        this.batteryHealthPartSeven = batteryHealthPartSeven;
    }

    public double getBatteryV() {
        return batteryV;
    }

    public void setBatteryV(double batteryV) {
        this.batteryV = batteryV;
    }

    public int getBatteryCoreNum() {
        return batteryCoreNum;
    }

    public void setBatteryCoreNum(int batteryCoreNum) {
        this.batteryCoreNum = batteryCoreNum;
    }

    public double getBatteryRemainCapacity() {
        return batteryRemainCapacity;
    }

    public void setBatteryRemainCapacity(double batteryRemainCapacity) {
        this.batteryRemainCapacity = batteryRemainCapacity;
    }

    public double getSoh() {
        return soh;
    }

    public void setSoh(double soh) {
        this.soh = soh;
    }

    public double getBatteryChargeA() {
        return batteryChargeA;
    }

    public void setBatteryChargeA(double batteryChargeA) {
        this.batteryChargeA = batteryChargeA;
    }

    public double getEnvTemp() {
        return envTemp;
    }

    public void setEnvTemp(double envTemp) {
        this.envTemp = envTemp;
    }

    public double getBatteryCoreTemp() {
        return batteryCoreTemp;
    }

    public void setBatteryCoreTemp(double batteryCoreTemp) {
        this.batteryCoreTemp = batteryCoreTemp;
    }

    public double getHardwareTemp() {
        return hardwareTemp;
    }

    public void setHardwareTemp(double hardwareTemp) {
        this.hardwareTemp = hardwareTemp;
    }

    public List<Double> getBatteryCoreVList() {
        return batteryCoreVList;
    }

    @Override
    public String toString() {
        return "BatteryOtherProperties{" +
                "batteryV=" + batteryV +
                ", batteryCoreNum=" + batteryCoreNum +
                ", batteryRemainCapacity=" + batteryRemainCapacity +
                ", soh=" + soh +
                ", batteryChargeA=" + batteryChargeA +
                ", envTemp=" + envTemp +
                ", batteryCoreTemp=" + batteryCoreTemp +
                ", hardwareTemp=" + hardwareTemp +
                ", batteryCoreVList=" + batteryCoreVList +
                ", batteryHealthPartOne='" + batteryHealthPartOne + '\'' +
                ", batteryHealthPartTwo='" + batteryHealthPartTwo + '\'' +
                ", batteryHealthPartThree='" + batteryHealthPartThree + '\'' +
                ", batteryHealthPartFour='" + batteryHealthPartFour + '\'' +
                ", batteryHealthPartFive='" + batteryHealthPartFive + '\'' +
                ", batteryHealthPartSix='" + batteryHealthPartSix + '\'' +
                ", batteryHealthPartSeven='" + batteryHealthPartSeven + '\'' +
                '}';
    }
}
