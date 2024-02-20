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
    
    MerchantAttr queryByMerchantId(Long merchantId);

    MerchantAttr queryByMerchantIdFromCache(Long merchantId);

    Integer updateByMerchantId(MerchantAttr merchantAttr);
    
    Integer insert(MerchantAttr merchantAttr);

    Integer deleteByMerchantId(Long merchantId);
    
    Triple<Boolean, String, Object> updateUpgradeCondition(Long merchantId, Integer condition);
    
    Triple<Boolean, String, Object> updateInvitationCondition(MerchantAttrRequest request);
    
    /**
     * 初始化商户升级配置
     * @param tenantId
     * @return
     */
    Integer initMerchantAttr(Long merchantId, Integer tenantId);
    
    /**
     * 校验邀请时间是否在有效期内
     */
    Boolean checkInvitationTime(Long merchantId, Long invitationTime);
}
