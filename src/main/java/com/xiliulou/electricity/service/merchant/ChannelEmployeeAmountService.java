package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/20 16:03
 */
public interface ChannelEmployeeAmountService {
    
    /**
     * 返现给渠道员时增加金额
     * @param amount
     * @param uid
     * @return
     */
    Integer addAmount(BigDecimal amount, Long uid, Long tenantId);
    
    /**
     * 退款时从渠道员账户表中扣除金额
     * @param amount
     * @param uid
     * @return
     */
    Integer reduceAmount(BigDecimal amount, Long uid, Long tenantId);
    
}
