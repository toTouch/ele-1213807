package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/08/01 14:22
 * @mood 首页押金
 */
@Data
public class HomePageDepositVo {

    /**
     * 电池押金
     */
    private BigDecimal batteryDeposit;

    /**
     * 车辆押金
     */
    private BigDecimal carDeposit;

    /**
     * 今日电池押金
     */
    private BigDecimal todayBatteryDeposit;

    /**
     * 今日车辆押金
     */
    private BigDecimal todayCarDeposit;

    /**
     * 今日缴纳押金
     */
    private BigDecimal todayPayDeposit;

    /**
     * 今日退押金
     */
    private BigDecimal todayRefundDeposit;
}
