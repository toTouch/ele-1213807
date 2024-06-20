package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.request.merchant.MerchantAreaSaveOrUpdateRequest;
import com.xiliulou.electricity.vo.merchant.MerchantAreaVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description 区域管理服务
 * @date 2024/2/6 13:51:23
 */
public interface MerchantAreaService {
    
    R save(MerchantAreaSaveOrUpdateRequest saveRequest, Long operator);
    
    R deleteById(Long id, Long bindFranchiseeId);
    
    R updateById(MerchantAreaSaveOrUpdateRequest updateRequest);
    
    List<MerchantAreaVO> listByPage(MerchantAreaRequest request);
    
    Integer countTotal(MerchantAreaRequest request);
    
    List<MerchantArea> listAll(MerchantAreaRequest request);
    
    List<MerchantArea> queryList(MerchantAreaRequest request);
    
    MerchantArea queryById(Long id);
}
