package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantPromotionRequest;
import com.xiliulou.electricity.vo.merchant.MerchantPromotionMonthRecordVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author HeYafeng
 * @description 商户推广费月度统计
 * @date 2024/2/24 10:25:15
 */
public interface MerchantPromotionMonthRecordService {
    
    List<MerchantPromotionMonthRecordVO> listByPage(MerchantPromotionRequest request);
    
    Integer countTotal(MerchantPromotionRequest request);
    
    void exportExcel(MerchantPromotionRequest request, HttpServletResponse response);
}
