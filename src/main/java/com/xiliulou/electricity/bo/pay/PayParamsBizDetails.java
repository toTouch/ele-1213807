/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.bo.pay;

import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/16 15:55
 */

@Data
public class PayParamsBizDetails {
    
    private Integer payType;
    
    /**
     * 微信支付参数
     */
    private WechatPayParamsDetails wechatPayParamsDetails;
    
    /**
     * 支付宝参数
     */
    private AlipayAppConfig alipayAppConfig;
    
}
