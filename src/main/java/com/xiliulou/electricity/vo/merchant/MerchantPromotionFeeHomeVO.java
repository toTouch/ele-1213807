package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

/**
 * @ClassName : MerchantPromotionFeeHomeVO
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
@Data
public class MerchantPromotionFeeHomeVO {
   
   /**
    * 收入
    */
   private MerchantPromotionFeeIncomeVO merchantPromotionFeeIncomeVO;
   
   /**
    * 人数
    */
   private MerchantPromotionFeeScanCodeVO merchantPromotionFeeScanCodeVO;
   
   /**
    * 次数
    */
   private MerchantPromotionFeeRenewalVO merchantPromotionFeeRenewalVO;
}
