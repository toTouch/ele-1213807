package com.xiliulou.electricity.query;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/4/14 14:02
 * @mood
 */
@Data
public class HomePageDepositQuery {
    
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
     * 线上车辆押金
     */
    private BigDecimal onlineCarDeposit;
    
    /**
     * 线下车辆押金
     */
    private BigDecimal offlineCarDeposit;
    
    /**
     * 免押车辆押金
     */
    private BigDecimal freeCarDeposit;
    
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
     * 今日线上车辆押金
     */
    private BigDecimal todayOnlineCarDeposit;
    
    /**
     * 今日线下车辆押金
     */
    private BigDecimal todayOfflineCarDeposit;
    
    /**
     * 今日免押车辆押金
     */
    private BigDecimal todayFreeCarDeposit;
    
    /**
     * 今日车辆总押金
     */
    private BigDecimal todayCarDeposit;
    
    
    /**
     * 今日总缴纳押金
     */
    private BigDecimal todayPayDeposit;
    
    
    /**
     * 今日电池线上退押金
     */
    private BigDecimal todayOnlineRefundDeposit;
    
    /**
     * 今日电池线下退押金
     */
    private BigDecimal todayOfflineRefundDeposit;
    
    /**
     * 今日电池免押退押金
     */
    private BigDecimal todayFreeRefundDeposit;
    
    /**
     * 今日电池总退押金
     */
    private BigDecimal todayRefundDeposit;
    
    /**
     * 今日车辆线上退押金
     */
    private BigDecimal todayOnlineCarRefundDeposit;
    
    /**
     * 今日车辆线下退押金
     */
    private BigDecimal todayOfflineCarRefundDeposit;
    
    /**
     * 今日车辆免押退押金
     */
    private BigDecimal todayFreeCarRefundDeposit;
    
    /**
     * 今日车辆总退押金
     */
    private BigDecimal todayCarRefundDeposit;
    
    
    /**
     * 历史线上退换电押金
     */
    private BigDecimal historyOnlineRefundBatteryDeposit;
    
    /**
     * 历史线下退换电押金
     */
    private BigDecimal historyOfflineRefundBatteryDeposit;
    
    /**
     * 历史免押退换电押金
     */
    private BigDecimal historyFreeRefundBatteryDeposit;
    
    /**
     * 历史总退换电押金
     */
    private BigDecimal historyRefundBatteryDeposit;
    
    
    /**
     * 历史线上退租车押金
     */
    private BigDecimal historyOnlineRefundCarDeposit;
    
    /**
     * 历史线下退租车押金
     */
    private BigDecimal historyOfflineRefundCarDeposit;
    
    /**
     * 历史免押退租车押金
     */
    private BigDecimal historyFreeRefundCarDeposit;
    
    /**
     * 历史总退租车押金
     */
    private BigDecimal historyRefundCarDeposit;
    
    /**
     * 总营业额
     */
    private BigDecimal sumDepositTurnover;
}
