package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.RebateConfig;
import com.xiliulou.electricity.request.merchant.RebateConfigRequest;
import com.xiliulou.electricity.vo.merchant.RebateConfigVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 返利配置表(RebateConfig)表服务接口
 *
 * @author zzlong
 * @since 2024-02-04 16:32:06
 */
public interface RebateConfigService {
    
    RebateConfig queryById(Long id);
    
    RebateConfig queryByIdFromCache(Long id);
    
    List<RebateConfigVO> listByPage(RebateConfigRequest rebateConfigRequest);
    
    RebateConfig insert(RebateConfig rebateConfig);
    
    Integer update(RebateConfig rebateConfig);
    
    Integer existsRebateConfigByMidAndLevel(Long mid, String level);
    
    Triple<Boolean, String, Object> modify(RebateConfigRequest request);
    
    Triple<Boolean, String, Object> save(RebateConfigRequest request);
    
    RebateConfig queryByMidAndMerchantLevel(Long memberCardId, String level);
    
    List<RebateConfig> listRebateConfigByMid(Long memberCardId);
    
    RebateConfig queryLatestByMidAndMerchantLevel(Long memberCardId, String level);
}
