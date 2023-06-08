package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarBatteryFreeDepositAlipayVo {
    private BigDecimal payAmount;
    private BigDecimal alipayAmount;
}
