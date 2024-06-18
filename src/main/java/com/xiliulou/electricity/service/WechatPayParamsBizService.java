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
     * 根据租户id + 加盟商id查询缓存<br/>
     * <p>
     * 1.franchiseeId 不存在配置,则返回运营商默认配置<br/> 2.franchiseeId 存在配置,则返回加盟商配置<br/> 3.如果要查询运营商默认配置，franchiseeId传{@link com.xiliulou.electricity.constant.MultiFranchiseeConstant#DEFAULT_FRANCHISEE}
     * </p>
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/6/12 11:16
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