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
    
    List<MerchantLevel> listByFranchiseeId(Integer tenantId, Long franchiseeId);
    
    MerchantLevel insert(MerchantLevel merchantLevel);
    
    Integer updateById(MerchantLevel merchantLevel);
    
    Integer deleteByFranchiseeId(Long franchiseeId);
    
    Integer initMerchantLevel(Long franchiseeId, Integer tenantId);
    
    Triple<Boolean, String, Object> modify(MerchantLevelRequest request);
    
    List<MerchantLevelVO> list(Integer tenantId, Long franchiseeId);
    
    MerchantLevel queryByMerchantLevelAndFranchiseeId(String level, Long franchiseeId);
    
    /**
     * 获取下一级商户等级
     *
     * @return
     */
    MerchantLevel queryNextByMerchantLevel(String level, Long franchiseeId);
    
    MerchantLevel queryLastByMerchantLevel(String level, Long franchiseeId);
    
    List<MerchantLevel> queryListByIdList(List<Long> levelIdList);
    
    Integer existsLevelName(String name, Long franchiseeId);
    
    List<MerchantLevel> listByTenantId(Integer tenantId);
}
