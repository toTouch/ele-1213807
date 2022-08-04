package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/08/01 14:22
 * @mood 首页柜机分析
 */
@Data
public class HomePageElectricityOrderVo {

    private Integer orderSuccessCount;

    private Integer sumOrderCount;

    private Integer onlineElectricityCabinet;

    private Integer offlineElectricityCabinet;
}
