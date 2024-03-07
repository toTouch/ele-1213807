package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindHistory;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/26 14:19
 * @desc
 */
public interface MerchantCabinetBindHistoryService {
    
    List<MerchantCabinetBindHistory> queryListByMonth(Long cabinetId, Long placeId, List<String> monthList, Long merchantId, Integer tenantId);
}
