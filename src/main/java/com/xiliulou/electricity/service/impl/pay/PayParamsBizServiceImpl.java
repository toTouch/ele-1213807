/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/16
 */

package com.xiliulou.electricity.service.impl.pay;

import com.xiliulou.electricity.bo.pay.PayParamsBizDetails;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.AlipayAppConfig;
import com.xiliulou.electricity.service.AlipayAppConfigService;
import com.xiliulou.electricity.service.WechatPayParamsBizService;
import com.xiliulou.electricity.service.pay.PayParamsBizService;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * description: 支付参数业务service
 *
 * @author caobotao.cbt
 * @date 2024/7/16 15:50
 */
@Service
public class PayParamsBizServiceImpl implements PayParamsBizService {
    
    
    @Resource
    private WechatPayParamsBizService wechatPayParamsBizService;
    
    @Resource
    private AlipayAppConfigService alipayAppConfigService;
    
    @Override
    public PayParamsBizDetails queryPayParams(Integer payType, Integer tenantId, Long franchiseeId) throws WechatPayException {
        PayParamsBizDetails payParamsBizDetails = new PayParamsBizDetails();
        payParamsBizDetails.setPayType(payType);
        if (1 == payType) {
            // TODO: 2024/7/16 CBT魔法值后期替换
            WechatPayParamsDetails wechatPayParamsDetails = wechatPayParamsBizService.getDetailsByIdTenantIdAndFranchiseeId(tenantId, franchiseeId);
            payParamsBizDetails.setWechatPayParamsDetails(wechatPayParamsDetails);
        } else {
            //获取支付宝配置
            AlipayAppConfig alipayAppConfig = alipayAppConfigService.queryByTenantIdAndFranchiseeId(tenantId, franchiseeId);
            payParamsBizDetails.setAlipayAppConfig(alipayAppConfig);
        }
        return payParamsBizDetails;
    }
}
