package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/07/27 14:22
 * @mood 首页营业额
 */
@Data
public class HomePageTurnOverGroupByWeekDayVo {

    /**
     * 总营业额
     */
    private BigDecimal turnover;

    /**
     * 日期
     */
    private String weekDate;


}
