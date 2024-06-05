package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantAttr;

/**
 * 商户升级配置(MerchantAttr)表数据库访问层
 *
 * @author zzlong
 * @since 2024-02-04 09:14:33
 */
public interface MerchantAttrMapper {

    MerchantAttr selectById(Long id);
    
    MerchantAttr selectByTenantId(Integer tenantId);

    int updateByTenantId(MerchantAttr merchantAttr);
    
    int insert(MerchantAttr merchantAttr);

    int deleteByTenantId(Integer tenantId);

}
