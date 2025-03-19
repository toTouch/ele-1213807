package com.xiliulou.electricity.mapper.thirdPartyMall;

import com.xiliulou.electricity.thirdparty.MeiTuanRiderMallConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 美团骑手商城配置信息
 * @date 2024/8/28 11:01:57
 */
public interface MeiTuanRiderMallConfigMapper {
    
    MeiTuanRiderMallConfig selectByTenantId(Integer tenantId);
    
    Integer insert(MeiTuanRiderMallConfig meiTuanRiderMallConfig);
    
    Integer update(MeiTuanRiderMallConfig meiTuanRiderMallConfig);
    
    MeiTuanRiderMallConfig selectByConfig(MeiTuanRiderMallConfig config);
    
    List<MeiTuanRiderMallConfig> selectListEnableMeiTuanRiderMall(@Param("offset") Integer offset, @Param("size") Integer size);
    
}
