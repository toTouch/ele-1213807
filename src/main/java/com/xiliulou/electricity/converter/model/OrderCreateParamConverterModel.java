/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/18
 */

package com.xiliulou.electricity.converter.model;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/18 09:37
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateParamConverterModel {
    
    /**
     * 交易订单(系统发起支付时的订单号,我们系统交易订单唯一)
     */
    private String orderId;
    
    /**
     * 失效时间
     */
    private Long expireTime;
    
    /**
     * 描述
     */
    private String description;
    
    
    /**
     * 自定义参数
     */
    private String attach;
    
    /**
     * 交易金额 (单位：元)
     */
    private BigDecimal amount;
    
    /**
     * 币种
     */
    private String currency;
    
    /**
     * 第三方id
     */
    private String openId;
    
    /**
     * 支付配置信息
     */
    private BasePayConfig payConfig;
    
}
