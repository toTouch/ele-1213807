package com.xiliulou.electricity.service.merchant;


import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonth;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthDetail;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/22 13:34
 * @desc
 */
public interface MerchantPlaceFeeMonthDetailService {
    
    Integer batchInsert(List<List<MerchantPlaceFeeMonthDetail>> partition);
    
    List<Long> queryMerchantIdList(Long startTime, Long endTime);
    
    List<MerchantPlaceFeeMonthDetail> queryListByMerchantId(Long merchantId, Long startTime, Long endTime);
    
    List<MerchantPlaceFeeMonthDetail> queryListByMonth(Long cabinetId, Long placeId, List<String> monthList);
}
