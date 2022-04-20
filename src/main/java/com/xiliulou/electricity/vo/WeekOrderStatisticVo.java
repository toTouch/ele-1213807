package com.xiliulou.electricity.vo;

import lombok.Data;
/**
 * @author hrp
 * @date 2022/3/23 15:37
 * @mood 周订单统计视图
 */
@Data
public class WeekOrderStatisticVo {

    /**
     * 日期
     */
    private String weekDate;

    /**
     * 数量
     */
    private Integer weekCount;

}
