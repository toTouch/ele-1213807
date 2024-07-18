/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/17
 */

package com.xiliulou.electricity.bo.base;

import lombok.Data;

import java.io.Serializable;

/**
 * description: 支付配置
 *
 * @author caobotao.cbt
 * @date 2024/7/17 10:54
 */
public abstract class BasePayConfig implements Serializable {
    
    
    /**
     * 租户id
     */
    public abstract Integer getTenantId();
    
    
    /**
     * 加盟商id
     */
    public abstract Long getFranchiseeId();
    
    
    /**
     * 第三方小程序的商户id
     */
    public abstract String getThirdPartyMerchantId();
    
    
    /**
     * 支付类型
     */
    public abstract String getPaymentMethod();
    
    
}
