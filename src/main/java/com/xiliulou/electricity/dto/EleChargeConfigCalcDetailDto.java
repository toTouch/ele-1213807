package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2023/7/18 11:32
 */
@Data
public class EleChargeConfigCalcDetailDto implements Comparable<EleChargeConfigCalcDetailDto> {
    /**
     * 电价类型 0--平用电 1--峰用电 3--谷用电
     */
    private Integer type;

    private Integer startHour;

    private Integer endHour;

    private Double price;


    @Override
    public int compareTo(EleChargeConfigCalcDetailDto o) {
        return Integer.compare(o.startHour, this.startHour);
    }
}
