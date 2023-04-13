package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/4/13 10:24
 * @mood
 */
@Data
public class FreeDepositAlipayHistoryVo {
    
    private Long id;
    
    private String orderId;
    
    private String name;
    
    private String phone;
    
    private String idCard;
    
    private String operateName;
    
    /**
     * 支付金额
     */
    private Double payAmount;
    
    /**
     * 代扣金额
     */
    private Double alipayAmount;
    
    private Integer type;
    
    private Long createTime;
    
    private Integer tenantId;
}
