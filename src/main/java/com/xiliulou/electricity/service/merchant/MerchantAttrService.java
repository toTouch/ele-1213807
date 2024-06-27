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
    
    MerchantAttr queryByTenantId(Integer tenantId);

    MerchantAttr queryByTenantIdFromCache(Integer tenantId);

    Integer updateByTenantId(MerchantAttr merchantAttr, Integer tenantId);
    
    Integer insert(MerchantAttr merchantAttr);

    Integer deleteByTenantId(Integer tenantId);
    
    Triple<Boolean, String, Object> updateUpgradeCondition(Integer tenantId, Integer condition);
    
    Triple<Boolean, String, Object> updateInvitationCondition(MerchantAttrRequest request);
    
    /**
     * 初始化商户升级配置
     * @param tenantId
     * @return
     */
    Integer initMerchantAttr(Integer tenantId);
    
    /**
     * 校验邀请时间是否在有效期内
     */
    Boolean checkInvitationTime(MerchantAttr merchantAttr, Long invitationTime);
    
    MerchantAttr queryUpgradeCondition(Integer tenantId);
    
    Triple<Boolean, String, Object> updateChannelSwitchState(Integer tenantId, Integer status);
}
