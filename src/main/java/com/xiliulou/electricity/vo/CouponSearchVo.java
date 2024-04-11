package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author SongJinpan
 * @description:
 * @Date 2024/4/9 15:56
 */

@Data
public class CouponSearchVo {
    
    private Long id;
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠金额
     */
    private BigDecimal amount;
}
