package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName : PromotionFeeStatisticAnalysisIncomeVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-22
 */
@Data
public class PromotionFeeStatisticAnalysisUserVO {
    private List<PromotionFeeStatisticAnalysisUserScanCodeVO> scanCodeVOList;
    
    private List<PromotionFeeStatisticAnalysisPurchaseVO> purchaseVOList;
    
    private List<PromotionFeeStatisticAnalysisRenewalVO> renewalVOList;
}
