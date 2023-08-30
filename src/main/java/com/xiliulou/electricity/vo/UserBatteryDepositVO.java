package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-04-10:59
 */
@Data
public class UserBatteryDepositVO {

    /**
     * 电池租赁状态 0--未租电池，1--已租电池
     */
    private Integer batteryRentStatus;

    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中,3--押金退款失败
     */
    private Integer batteryDepositStatus;

    private BigDecimal batteryDeposit;

    /**
     * 押金类型
     */
    private Integer depositType;



}
