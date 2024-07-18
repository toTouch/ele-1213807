/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.pay;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.pay.alipay.exception.AliPayException;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;

/**
 * description: 支付配置查询业务层
 *
 * @author caobotao.cbt
 * @date 2024/7/16 15:44
 */
public interface PayConfigBizService {
    
    /**
     * 支付参数查询
     * <p>
     * 1.franchiseeId 不存在配置,则返回运营商默认配置<br/> 2.franchiseeId 存在配置,则返回加盟商配置<br/> 3.如果要查询运营商默认配置，franchiseeId传{@link com.xiliulou.electricity.constant.MultiFranchiseeConstant#DEFAULT_FRANCHISEE}
     * </p>
     *
     * @param paymentMethod 支付方式 {@link com.xiliulou.electricity.enums.PaymentMethodEnum}
     * @param tenantId      租户id
     * @param franchiseeId  加盟商id
     * @return
     * @author caobotao.cbt
     * @date 2024/7/16 15:46
     */
    <T extends BasePayConfig> PayParamsBizDetails<T> queryPayParams(String paymentMethod, Integer tenantId, Long franchiseeId) throws WechatPayException, AliPayException;
    
    
    /**
     * 支付参数查询 （精确查询，传入的franchiseeId是什么，就查什么）
     *
     * @param paymentMethod
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/7/18 14:36
     */
    <T extends BasePayConfig> PayParamsBizDetails<T> queryPrecisePayParams(String paymentMethod, Integer tenantId, Long franchiseeId) throws WechatPayException, AliPayException;
    
}