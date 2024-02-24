package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPromotionMonthRecord;
import com.xiliulou.electricity.query.merchant.MerchantPromotionMonthRecordQueryModel;

import java.util.List;

/**
 * @author HeYafeng
 * @description 商户推广费月度统计
 * @date 2024/2/24 10:26:32
 */
public interface MerchantPromotionMonthRecordMapper {
    
    List<MerchantPromotionMonthRecord> selectListByPage(MerchantPromotionMonthRecordQueryModel queryModel);
    
    Integer countTotal(MerchantPromotionMonthRecordQueryModel queryModel);
}
