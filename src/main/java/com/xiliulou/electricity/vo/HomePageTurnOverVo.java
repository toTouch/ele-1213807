package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/07/27 14:22
 * @mood 首页营业额
 */
@Data
public class HomePageTurnOverVo {

    /**
     * 总营业额
     */
    private BigDecimal sumTurnover;

    /**
     * 电池月卡营业额
     */
    private BigDecimal batteryMemberCardTurnover;

    /**
     * 车辆套餐营业额
     */
    private BigDecimal carMemberCardTurnover;

    /**
     * 电池服务费营业额
     */
    private BigDecimal batteryServiceFeeTurnover;


    /**
     * 今日电池月卡营业额
     */
    private BigDecimal todayBatteryMemberCardTurnover;

    /**
     * 今日车辆套餐营业额
     */
    private BigDecimal todayCarMemberCardTurnover;

    /**
     * 今日电池服务费营业额
     */
    private BigDecimal todayBatteryServiceFeeTurnover;



    /**
     * 今日营业额
     */
    private BigDecimal todayTurnover;
}
