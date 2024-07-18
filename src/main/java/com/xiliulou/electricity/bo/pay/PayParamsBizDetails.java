/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.bo.pay;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import lombok.Data;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/16 15:55
 */

@Data
public class PayParamsBizDetails<T extends BasePayConfig> {
    /**
     * 支付方式
     *
     * @see com.xiliulou.electricity.enums.PaymentMethodEnum
     */
    private String paymentMethod;
    
    /**
     * 支付参数
     */
    private T payParamConfig;
    
}
