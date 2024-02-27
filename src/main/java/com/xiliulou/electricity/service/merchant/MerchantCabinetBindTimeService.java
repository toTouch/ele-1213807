package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantCabinetBindTime;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/26 18:45
 * @desc
 */
public interface MerchantCabinetBindTimeService {
    
    List<MerchantCabinetBindTime> queryListByMerchantId(Long merchantId, Long cabinetId, Long placeId);
}
