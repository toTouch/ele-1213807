package com.xiliulou.electricity.mq.model;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-18-16:53
 */
@Data
public class MerchantUpgrade {
    
    private Long merchantId;
    
    private Long uid;
    
    private String orderId;
}
