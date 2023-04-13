package com.xiliulou.electricity.query;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

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
    
    private String userName;
    
    private String phone;
    
    private String idCard;
    
    private Integer type;
    
    private Long beginTime;
    
    private String endTime;
    
    private Integer tenantId;
}
