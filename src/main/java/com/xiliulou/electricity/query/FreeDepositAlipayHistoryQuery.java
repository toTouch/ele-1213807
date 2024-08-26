package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * @author zgw
 * @date 2023/4/13 10:19
 * @mood
 */
@Data
public class FreeDepositAlipayHistoryQuery {
    
    private Long size;
    
    private Long offset;
    
    private String orderId;
    
    private String authPayOrderId;
    
    private String userName;
    
    private String phone;
    
    private String idCard;
    
    private Integer type;
    
    private Long beginTime;
    
    private Long endTime;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
}
