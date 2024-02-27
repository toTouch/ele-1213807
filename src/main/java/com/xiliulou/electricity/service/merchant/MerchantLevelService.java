package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantLevel;
import com.xiliulou.electricity.request.merchant.MerchantLevelRequest;
import com.xiliulou.electricity.vo.merchant.MerchantLevelVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 商户等级表(MerchantLevel)表服务接口
 *
 * @author zzlong
 * @since 2024-02-04 14:35:06
 */
public interface MerchantLevelService {

    MerchantLevel queryById(Long id);

    List<MerchantLevel> listByTenantId(Integer tenantId);

    MerchantLevel insert(MerchantLevel merchantLevel);

    Integer updateById(MerchantLevel merchantLevel);
    
    Integer deleteById(Long id);
    
    Integer initMerchantLevel(Integer tenantId);
    
    Triple<Boolean, String, Object> modify(MerchantLevelRequest request);
    
    List<MerchantLevelVO> list(Integer tenantId);
    
    /**
     * 获取下一级商户等级
     * @param level
     * @param tenantId
     * @return
     */
    MerchantLevel queryNextByMerchantLevel(String level, Integer tenantId);
    MerchantLevel queryLastByMerchantLevel(String level, Integer tenantId);
    
    List<MerchantLevel> queryListByIdList(List<Long> levelIdList);
}
