package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/2/15 14:06
 */
@Data
public class FreeDepositUserInfoVo {
    private String idCard;
    private String phone;
    private String name;

    /**
     * 加盟商
     */
    private Long franchiseeId;

    /**
     * 电池型号
     */
    private Integer model;

    /**
     * 电池押金类型
     */
    private Integer batteryDepositType;

    /**
     * 缴纳电池押金的时间
     */
    private Long applyBatteryDepositTime;

    /**
     * 电池押金冻结状态
     */
    private Integer batteryDepositAuthStatus;

    /**
     * 车辆型号
     */
    private Long carModel;

    /**
     * 缴纳车辆押金的时间
     */
    private Long applyCarDepositTime;

    /**
     * 车辆押金冻结状态
     */
    private Integer carDepositAuthStatus;

    /**
     * 车辆电池押金冻结状态
     */
    private Integer carBatteryDepositAuthStatus;

    /**
     * 缴纳车辆电池押金的时间
     */
    private Long applyCarBatteryDepositTime;
    
    /**
     * 是否是免押
     */
    private Integer isFreeDeposit;


    public static final Integer FREE_DEPOSIT_CLOSE = 0;
    public static final Integer FREE_DEPOSIT_OPEN = 1;
    
    
    public static final Integer FREE_DEPOSIT_ORDER = 1;
}
