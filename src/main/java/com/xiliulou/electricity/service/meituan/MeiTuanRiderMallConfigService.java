package com.xiliulou.electricity.service.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;
import com.xiliulou.electricity.request.meituan.MeiTuanRiderMallConfigRequest;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息服务接口
 * @date 2024/8/28 10:31:51
 */
public interface MeiTuanRiderMallConfigService {
    
    Integer insertOrUpdate(MeiTuanRiderMallConfigRequest meiTuanRiderMallConfigRequest);
    
    MeiTuanRiderMallConfig queryByTenantIdFromCache(Integer tenantId);
    
    MeiTuanRiderMallConfig queryByTenantId(Integer tenantId);
    
    MeiTuanRiderMallConfig queryByConfig(MeiTuanRiderMallConfig config);
    
    MeiTuanRiderMallConfig queryByConfigFromCache(MeiTuanRiderMallConfig config);
    
    MeiTuanRiderMallConfig checkEnableMeiTuanRiderMall(Integer tenantId);
}
