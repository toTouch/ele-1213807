package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:46
 */
public interface MerchantWithdrawApplicationService {
    
    Integer saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    Integer updateMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    Integer removeMerchantWithdrawApplication(Long id);
    
    Integer countMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    List<MerchantWithdrawApplication> queryMerchantWithdrawApplicationList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    MerchantWithdrawApplication queryMerchantWithdrawApplication(Long id);
    
}
