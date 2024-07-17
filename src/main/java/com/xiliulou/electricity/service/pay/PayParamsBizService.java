/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.pay;

import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;

/**
 * description: 支付配置查询业务层
 *
 * @author caobotao.cbt
 * @date 2024/7/16 15:44
 */
public interface PayParamsBizService {
    
    /**
     * 支付参数查询
     *
     * @param payType
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/7/16 15:46
     * @return
     */
    PayParamsBizDetails queryPayParams(Integer payType, Integer tenantId, Long franchiseeId) throws WechatPayException;
    
}