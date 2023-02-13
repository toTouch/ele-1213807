package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/2/13 14:25
 * @mood
 */
@Data
public class CarModelPullVo {
    
    private Long id;
    
    private String name;
    
    private BigDecimal carDeposit;
    
    private String rentType;
}
