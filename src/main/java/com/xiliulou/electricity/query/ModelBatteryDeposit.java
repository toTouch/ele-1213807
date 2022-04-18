package com.xiliulou.electricity.query;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单表(TElectricityCabinetOrder)实体类
 *
 * @author makejava
 * @since 2020-11-26 16:00:45
 */
@Data
public class ModelBatteryDeposit {

    /**
     *电池类型
     */
    private Integer model;

    /**
     * 租电池押金
     */
    private BigDecimal batteryDeposit;

    /**
     * 电池服务费
     */
    private BigDecimal batteryServiceFee;




}
