package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeekTurnoverStatisticVo {

    private String weekDate;

    private BigDecimal turnover;
}
