package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantAttr;
import com.xiliulou.electricity.request.merchant.MerchantAttrRequest;
import org.apache.commons.lang3.tuple.Triple;

/**
 * 商户升级配置(MerchantAttr)表服务接口
 *
 * @author zzlong
 * @since 2024-02-04 09:14:33
 */
public interface MerchantAttrService {
    
    MerchantAttr queryById(Long id);
    
    MerchantAttr queryByFranchiseeId(Long franchiseeId);
    
    MerchantAttr queryByFranchiseeIdFromCache(Long franchiseeId);
    
    Integer updateByFranchiseeId(MerchantAttr merchantAttr, Long franchiseeId);
    
    Integer insert(MerchantAttr merchantAttr);
    
    Integer deleteByFranchiseeId(Long franchiseeId);
    
    Triple<Boolean, String, Object> updateUpgradeCondition(Long franchiseeId, Integer condition);
    
    Triple<Boolean, String, Object> updateInvitationCondition(MerchantAttrRequest request);
    
    /**
     * 初始化商户升级配置
     *
     * @param tenantId
     * @return
     */
    Integer initMerchantAttr(Long franchiseeId, Integer tenantId);
    
    /**
     * 校验邀请时间是否在有效期内
     */
    Boolean checkInvitationTime(MerchantAttr merchantAttr, Long invitationTime);
    
    MerchantAttr queryUpgradeCondition(Long franchiseeId);
    
    Triple<Boolean, String, Object> updateChannelSwitchState(Long franchiseeId, Integer status);
}
