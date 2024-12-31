package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author HeYafeng
 * @description 退押
 * @date 2024/12/30 16:41:26
 */
@Data
public class EleDepositRefundVO {
    
    private Integer status;
    
    private Integer orderType;
    
    private Boolean refundFlag;
    
    private String orderId;
}
