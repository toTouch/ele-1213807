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
     * 今日押金
     */
    private BigDecimal todayPayDeposit;
    
    /**
     * 今日免押押金
     */
    private BigDecimal todayFreeDeposit;
    
    /**
     * 今日总押金
     */
    private BigDecimal todayDeposit;
    
    /**
     * 电池押金
     */
    private BigDecimal payBatteryDeposit;
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
    private BigDecimal payCarDeposit;
    /**
     * 免押车辆押金
     */
    private BigDecimal freeCarDeposit;
    /**
     * 车辆押金
     */
    private BigDecimal carDeposit;
    /**
     * 今日退押金
     */
    private BigDecimal todayPayRefundDeposit;
    /**
     * 今日免押退押金
     */
    private BigDecimal todayFreeRefundDeposit;
    /**
     * 今日总退押金
     */
    private BigDecimal todayRefundDeposit;
    /**
     * 总营业额
     */
    private BigDecimal sumDepositTurnover;
}
