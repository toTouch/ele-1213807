package com.xiliulou.electricity.query.meituan;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description
 * @date 2024/8/29 15:04:09
 */
@Data
@Builder
public class OrderQuery {
    
    private Long size;
    
    private Long offset;
    
    private Integer tenantId;
    
    private Long uid;
    
    private String phone;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 美团订单套餐订单ID
     */
    private String orderId;
    
}
