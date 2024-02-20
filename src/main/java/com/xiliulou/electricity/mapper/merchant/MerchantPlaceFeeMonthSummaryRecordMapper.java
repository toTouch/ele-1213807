package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeMonthSummaryRecord;
import org.springframework.stereotype.Repository;

/**
 * @ClassName : MerchantPlaceFeeMonthSummaryRecordMapper
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-19
 */
@Repository
public interface MerchantPlaceFeeMonthSummaryRecordMapper {
    Integer save(MerchantPlaceFeeMonthSummaryRecord record);
}
