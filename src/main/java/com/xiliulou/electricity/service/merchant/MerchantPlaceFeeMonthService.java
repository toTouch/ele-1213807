package com.xiliulou.electricity.service.merchant;


import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonth;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/22 11:41
 * @desc
 */
public interface MerchantPlaceFeeMonthService {
    
    Integer batchInsert(List<MerchantPlaceFeeMonth> item);
    
    List<Long> selectCabinetIdByMerchantId(Long merchantId);
    
    Integer existPlaceFeeByMerchantId(Long merchantId);
    
    List<MerchantPlaceFeeMonth> queryListByMonth(Long placeId, Long cabinetId, List<String> xDataList);
    
    List<MerchantPlaceFeeMonth> queryListByMerchantId(Long merchantId, Long cabinetId, Long placeId);
    
    BigDecimal sumFeeByTime(Long merchantId, Long placeId, Long cabinetId, Long time);
}
