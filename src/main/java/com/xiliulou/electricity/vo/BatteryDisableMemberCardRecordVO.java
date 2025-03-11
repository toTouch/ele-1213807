package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * @author : renhang
 * @description BatteryDisableMemberCardRecordVO
 * @date : 2025-02-26 15:20
 **/
@Data
public class BatteryDisableMemberCardRecordVO {

    private Long uid;

    private String disableMemberCardNo;

    /**
     * 冻结时间
     */
    private Long disableTime;

    /**
     * 月卡剩余天数
     */
    private Integer cardDays;

    /**
     * 用户选择的停卡天数
     */
    private Integer chooseDays;

    /**
     * 实际冻结天数
     */
    private Integer disableDays;

    /**
     * 解冻时间
     */
    private Long enableTime;

}
