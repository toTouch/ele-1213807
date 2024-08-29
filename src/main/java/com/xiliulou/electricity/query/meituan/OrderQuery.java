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
    
    private String orderId;
    
    /**
     * 用于判断当前时间与上次定时任务执行时间的差值，如果大于这个值，则从美团拉取近5分钟的订单
     */
    private Integer gapSecond;
    
    /**
     * 最近N分钟：当需要主动拉取美团订单时，拉取的时间范围
     */
    private Integer recentMinute;
    
}
