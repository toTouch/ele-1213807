package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthRecord;

import java.util.List;

/**
 * @ClassName : MerchantPlaceFeeMonthRecordService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
public interface MerchantPlaceFeeMonthRecordService {
    List<MerchantPlaceFeeMonthRecord> selectByMonthDate(String monthDate, Integer tenantId, Long franchiseeId);
    
    List<MerchantPlaceFeeMonthRecord> queryList(List<Long> placeIdList, List<String> monthList);
}
