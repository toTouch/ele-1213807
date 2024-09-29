package com.xiliulou.electricity.service.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息服务接口
 * @date 2024/8/28 10:31:51
 */
public interface MeiTuanRiderMallConfigService {
    
    MeiTuanRiderMallConfig queryByTenantIdFromCache(Integer tenantId);
    
    MeiTuanRiderMallConfig queryByTenantId(Integer tenantId);
    
}
