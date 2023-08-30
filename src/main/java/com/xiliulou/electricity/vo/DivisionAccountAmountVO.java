package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: Kenneth
 * @Date: 2023/7/25 14:00
 * @Description:
 */

@Data
public class DivisionAccountAmountVO {

    /**
     * 运营商收益
     */
    private BigDecimal operatorIncome;

    /**
     * 加盟商收益
     */
    private BigDecimal franchiseeIncome;

    /**
     * 门店收益
     */
    private BigDecimal storeIncome;

    /**
     * 运营商收益率
     */
    private BigDecimal operatorRate;

    /**
     * 加盟商收益率
     */
    private BigDecimal franchiseeRate;

    /**
     * 门店收益率
     */
    private BigDecimal storeRate;


}
