package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/19 17:39
 */
@Data
public class ElePowerMonthRecordVo {
    private String sn;

    private String eName;


    private String storeName;


    private String franchiseeName;
    /**
     * 月初耗电量
     */
    private Double monthStartPower;
    /**
     * 月末耗电量
     */
    private Double monthEndPower;
    /**
     * 本月耗电量
     */
    private Double monthSumPower;
    /**
     * 本月电费
     */
    private Double monthSumCharge;
    /**
     * 类别明细
     */
    private String jsonCharge;
    /**
     * 日期
     */
    private Object date;


    private Long createTime;
}
