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
     * 线上电池押金
     */
    private BigDecimal onlineBatteryDeposit;
    
    /**
     * 线下电池押金
     */
    private BigDecimal offlineBatteryDeposit;
    
    /**
     * 免押电池押金
     */
    private BigDecimal freeBatteryDeposit;

    /**
     * 电池总押金
     */
    private BigDecimal batteryDeposit;

    /**
     * 车辆押金
     */
    private BigDecimal carDeposit;
    
    /**
     * 今日线上电池押金
     */
    private BigDecimal todayOnlineBatteryDeposit;
    
    /**
     * 今日线下电池押金
     */
    private BigDecimal todayOfflineBatteryDeposit;
    
    /**
     * 今日免押电池押金
     */
    private BigDecimal todayFreeBatteryDeposit;

    /**
     * 今日电池总押金
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

    /**
     * 历史退换电押金
     */
    private BigDecimal historyRefundBatteryDeposit;

    /**
     * 历史退租车押金
     */
    private BigDecimal historyRefundCarDeposit;

    /**
     * 总营业额
     */
    private BigDecimal sumDepositTurnover;
}
