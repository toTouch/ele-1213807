/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/17
 */

package com.xiliulou.electricity.bo.base;


import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingReceiverConfig;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigReceiverStatusEnum;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigStatusEnum;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public abstract String getPaymentChannel();
    
    /**
     * 获取可用的分账接收方支付配置
     *
     * @author caobotao.cbt
     * @date 2024/8/27 10:39
     */
    
    /**
     * 获取可用的分账接收方支付配置
     *
     * @author caobotao.cbt
     * @date 2024/8/27 10:39
     */
    public abstract List<ProfitSharingReceiverConfig> getEnableProfitSharingReceiverConfigs();
    
    /**
     * 获取可用分账配置
     *
     * @author caobotao.cbt
     * @date 2024/8/28 17:50
     */
    public abstract ProfitSharingConfig getEnableProfitSharingConfig();
    
    /**
     * 配置类型
     */
    public abstract Integer getConfigType();
    
    
}
