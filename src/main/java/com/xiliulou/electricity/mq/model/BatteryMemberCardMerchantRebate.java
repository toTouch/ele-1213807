package com.xiliulou.electricity.mq.model;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-02-19-14:59
 */
@Data
public class BatteryMemberCardMerchantRebate {
    
    private String orderId;
    
    private Long uid;
    
    /**
     * 类型 0：购买套餐，1：退租
     */
    private Integer type;
}
