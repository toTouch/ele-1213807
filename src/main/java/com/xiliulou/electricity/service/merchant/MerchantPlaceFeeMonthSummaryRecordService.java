package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthSummaryRecord;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeMonthSummaryRecordQueryModel;

import java.util.List;

/**
 * @ClassName : MerchantPlaceFeeMonthSummaryRecordService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
public interface MerchantPlaceFeeMonthSummaryRecordService {
    List<MerchantPlaceFeeMonthSummaryRecord> selectByCondition(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel);
    
    Integer pageCountByCondition(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel);
}
