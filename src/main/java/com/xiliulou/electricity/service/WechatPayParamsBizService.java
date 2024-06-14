/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/6/14
 */

package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;

/**
 * description: 微信支付配置业务查询接口
 *
 * @author caobotao.cbt
 * @date 2024/6/14 12:58
 */
public interface WechatPayParamsBizService {
    
    /**
     * 根据租户+加盟商查询
     *
     * @param tenantId
     * @param franchiseeId
     * @return
     * @author caobotao.cbt
     * @date 2024/6/14 13:01
     */
    WechatPayParamsDetails getDetailsByIdTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId);
    
    
    /**
     * 更新缓存
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/6/14 13:22
     */
    void refreshMerchantInfo(Integer tenantId, Long franchiseeId);
    
}