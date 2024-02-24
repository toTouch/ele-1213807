package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:46
 */
public interface MerchantWithdrawApplicationService {

    Triple<Boolean, String, Object> saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);

    Triple<Boolean, String, Object> updateMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    Integer removeMerchantWithdrawApplication(Long id);
    
    Integer countMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    List<MerchantWithdrawApplicationVO> queryMerchantWithdrawApplicationList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    MerchantWithdrawApplication queryMerchantWithdrawApplication(Long id);
    
}
