package com.xiliulou.electricity.dto;

import lombok.Data;

@Data
public class EleMonthPowerGroupDto {
    private Integer type;
    private Double sumPower;
    private Double sumCharge;
}