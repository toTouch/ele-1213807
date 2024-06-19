package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeMonthSummaryRecordQueryModel;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName : MerchantPlaceFeeSettlementService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-20
 */
public interface MerchantPlaceFeeSettlementService {
    void export(String monthDate, HttpServletResponse response, Long franchiseeId);
    
    R page(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel);
    
    R pageCount(MerchantPlaceFeeMonthSummaryRecordQueryModel queryModel);
}
