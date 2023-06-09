package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-17-16:35
 */
@Data
public class EleCabinetOrderAnalyseVO {
    /**
     * 日均换电次数
     */
    private Double averageExchangeNumber = 0d;

    /**
     * 日均活跃度
     */
    private Double averagePeopleNumber = 0d;

    /**
     * 今日换电数
     */
    private Integer exchangeNumber = 0;

    /**
     * 今日活跃人数
     */
    private Integer peopleNumber = 0;
}
