package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.PxzConfig;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: FreeDepositOrderStatusDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-22 10:55
 */
@Data
@Builder
public class FreeDepositOrderStatusDTO {
    
    /**
     * 免押订单号
     */
    private String orderId;
    
    private Long uid;
    
    private PxzConfig pxzConfig;
    
    /**
     * 免押渠道
     */
    private Integer channel;
}
