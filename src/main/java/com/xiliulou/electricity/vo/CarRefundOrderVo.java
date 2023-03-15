package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zgw
 * @date 2023/3/15 17:45
 * @mood
 */
@Data
public class CarRefundOrderVo {
    
    private Long id;
    
    private String orderId;
    
    private String name;
    
    private String phone;
    
    private String carSn;
    
    private BigDecimal carDeposit;
    
    private String carModelName;
    
    /**
     * 1--审核中 2--审核通过 3--审核拒绝
     */
    private Integer status;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;
    
    private String storeName;
}
