

package com.xiliulou.electricity.service;

import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.enums.profitsharing.ProfitSharingQueryDetailsEnum;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;

import java.util.List;
import java.util.Set;

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
    WechatPayParamsDetails getDetailsByIdTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId) throws WechatPayException;
    
    
    /**
     * 根据租户id + 加盟商id查询缓存,<br/>
     * <p>
     * 1.franchiseeId 不存在配置,则返回运营商默认配置<br/> 2.franchiseeId 存在配置,则返回加盟商配置<br/> 3.如果要查询运营商默认配置，franchiseeId传{@link com.xiliulou.electricity.constant.MultiFranchiseeConstant#DEFAULT_FRANCHISEE}
     * </p>
     *
     * @param tenantId                 租户id
     * @param franchiseeId             加盟商id
     * @param queryProfitSharingConfig 查询的分账配置枚举
     * @author caobotao.cbt
     * @date 2024/8/27 08:37
     */
    WechatPayParamsDetails getDetailsByIdTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws WechatPayException;
    
    
    /**
     * 根据租户+加盟商id精确查询
     *
     * @param tenantId                 租户id
     * @param franchiseeId             加盟商id
     * @param queryProfitSharingConfig 查询的分账配置枚举
     * @author caobotao.cbt
     * @date 2024/8/27 08:37
     */
    WechatPayParamsDetails getPreciseCacheByTenantIdAndFranchiseeId(Integer tenantId, Long franchiseeId, Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig)
            throws WechatPayException;
    
    
    /**
     * 根据租户+加盟商id集合精确查询
     *
     * @param tenantId                 租户id
     * @param franchiseeIds            加盟商id集合
     * @param queryProfitSharingConfig 查询的分账配置枚举
     * @author caobotao.cbt
     * @date 2024/8/27 08:37
     */
    List<WechatPayParamsDetails> queryListPreciseCacheByTenantIdAndFranchiseeIds(Integer tenantId, Set<Long> franchiseeIds,
            Set<ProfitSharingQueryDetailsEnum> queryProfitSharingConfig) throws WechatPayException;
    
    
    /**
     * 更新缓存
     *
     * @author caobotao.cbt
     * @date 2024/6/14 13:22
     */
    void refreshMerchantInfo(Integer tenantId, Long franchiseeId);
    
}