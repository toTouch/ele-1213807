/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.pay;

import com.xiliulou.electricity.bo.base.BasePayConfig;
import com.xiliulou.electricity.enums.PaymentChannelEnum;
import com.xiliulou.pay.base.exception.PayException;

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
     * @param paymentChannel 支付方式 {@link PaymentChannelEnum}
     * @param tenantId       租户id
     * @param franchiseeId   加盟商id
     * @return
     * @author caobotao.cbt
     * @date 2024/7/16 15:46
     */
    BasePayConfig queryPayParams(String paymentChannel, Integer tenantId, Long franchiseeId) throws PayException;
    
    
    /**
     * 支付参数查询 （精确查询，传入的franchiseeId是什么，就查什么）
     *
     * @param paymentChannel
     * @param tenantId
     * @param franchiseeId
     * @return
     * @author caobotao.cbt
     * @date 2024/7/18 14:36
     */
    BasePayConfig queryPrecisePayParams(String paymentChannel, Integer tenantId, Long franchiseeId) throws PayException;
    
    
    /**
     * 校验入参配置是否与现有配置一致
     *
     * @param paymentChannel
     * @param tenantId
     * @param franchiseeId
     * @param thirdPartyMerchantId
     * @author caobotao.cbt
     * @date 2024/7/22 09:12
     */
    boolean checkConfigConsistency(String paymentChannel, Integer tenantId, Long franchiseeId, String thirdPartyMerchantId);
    
}