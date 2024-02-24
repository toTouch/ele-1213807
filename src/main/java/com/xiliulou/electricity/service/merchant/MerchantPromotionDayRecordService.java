package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.query.merchant.MerchantPromotionDayRecordQueryModel;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMonthDetailVO;

import java.util.List;

/**
 * @author HeYafeng
 * @description saas端商户推广费日结统计
 * @date 2024/2/23 20:50:44
 */
public interface MerchantPromotionDayRecordService {
    
    List<MerchantPromotionMonthDetailVO> listByTenantId(MerchantPromotionDayRecordQueryModel queryModel);
    
}
