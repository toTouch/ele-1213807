package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/07/27 14:22
 * @mood 首页用户
 */
@Data
public class HomePageUserByWeekDayVo {

    /**
     * 人数
     */
    private Integer userCount;

    /**
     * 日期
     */
    private String weekDate;


}
