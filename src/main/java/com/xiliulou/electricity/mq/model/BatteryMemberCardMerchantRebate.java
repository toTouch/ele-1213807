package com.xiliulou.electricity.mq.model;

import lombok.Data;

@Data
public class BatteryMemberCardMerchantRebate {
    
    private String orderId;
    
    private Long uid;
    
    /**
     * 类型 0：购买套餐，1：退租
     */
    private Integer type;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 流失用户 0：是, 1：否
     */
    private Integer lostUserType;
    
}
