package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/15 16:46
 * @mood
 */
@Data
@Builder
public class CarRefundOrderQuery {
    
    private Long offset;
    
    private Long size;
    
    private String orderId;
    
    private String userName;
    
    private String phone;
    
    private Integer tenantId;
    
    private Long storeId;
}
