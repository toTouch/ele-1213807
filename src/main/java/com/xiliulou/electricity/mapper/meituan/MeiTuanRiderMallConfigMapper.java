package com.xiliulou.electricity.mapper.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallConfig;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息
 * @date 2024/8/28 11:01:57
 */
public interface MeiTuanRiderMallConfigMapper {
    
    MeiTuanRiderMallConfig selectByTenantId(Integer tenantId);
    
}
