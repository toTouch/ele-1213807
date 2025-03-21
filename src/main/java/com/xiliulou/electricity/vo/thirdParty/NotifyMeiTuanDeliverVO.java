package com.xiliulou.electricity.vo.thirdParty;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @description 通知美团发货结果
 * @date 2024/9/29 19:05:27
 */
@Builder
@Data
public class NotifyMeiTuanDeliverVO {
    
    private Integer tenantId;
    
    private String orderId;
    
    private String resultStr;
    
    /**
     * true-发货成功 false-发货失败（订单取消）
     */
    private Boolean result;
    
    /**
     * 失败原因
     */
    private Integer failReason;
}
