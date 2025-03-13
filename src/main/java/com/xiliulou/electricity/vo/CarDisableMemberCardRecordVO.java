package com.xiliulou.electricity.vo;


import lombok.Data;

/**
 * @author : renhang
 * @description BatteryDisableMemberCardRecordVO
 * @date : 2025-02-26 15:20
 **/
@Data
public class CarDisableMemberCardRecordVO {

    private Long uid;

    private String disableMemberCardNo;

    /**
     * 用户选择的停卡天数
     */
    private Integer chooseDays;

    /**
     * 实际冻结天数
     */
    private Integer disableDays;

    /**
     * 冻结时间
     */
    private Long disableTime;

    /**
     * 解冻时间
     */
    private Long enableTime;

    /**
     * 租车套餐类型 1-单车、2-车电一体
     */
    private Integer rentalPackageType;
    /**
     * 月卡余量
     */
    private Long residue;

    /**
     * 余量单位，-1-次数、0-分钟、1-天
     */
    private Integer residueUnit;
}
