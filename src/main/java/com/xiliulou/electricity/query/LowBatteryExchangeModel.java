package com.xiliulou.electricity.query;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 配置表(TElectricityConfig)实体类
 *
 * @author makejava
 * @since 2022-07-05 16:00:45
 */
@Data
public class LowBatteryExchangeModel {

    /**
     * 换电开始时间
     */
    private Long exchangeBeginTime;

    /**
     * 换电结束时间
     */
    private Long exchangeEndTime;

    /**
     * 电量标准
     */
    private Integer batteryPowerStandard;




}
