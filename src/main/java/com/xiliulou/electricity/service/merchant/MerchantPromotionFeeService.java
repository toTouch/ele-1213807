package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.query.merchant.MerchantPromotionDataDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailSpecificsQueryModel;

/**
 * @ClassName : MerchantPromotionFeeService
 * @Description :
 * @Author : zhangyongbo
 * @since: 2024-02-21
 */
public interface MerchantPromotionFeeService {
    
    R queryMerchantAvailableWithdrawAmount(Long uid);
    
    R queryMerchantPromotionFeeIncome(Integer type, Long uid, Integer userType);
    
    R queryMerchantPromotionScanCode(Integer type, Long uid,Integer userType);
    
    R queryMerchantPromotionRenewal(Integer type, Long uid, Integer userType);
    
    R statisticMerchantIncome(Integer type, Long uid, Long beginTime, Long endTime,Integer userType);
    
    R statisticUser(Integer type, Long uid, Long beginTime, Long endTime, Integer userType);
    
    R statisticChannelEmployeeMerchant(Integer type, Long uid, Long beginTime, Long endTime);
    
    R selectMerchantEmployeeDetailList(MerchantPromotionEmployeeDetailQueryModel queryModel);
    
    R selectPromotionDataDetail(MerchantPromotionDataDetailQueryModel queryModel);
    
    R selectPromotionData(MerchantPromotionDataDetailQueryModel queryModel);
    
    R selectPromotionEmployeeDetailList(MerchantPromotionEmployeeDetailSpecificsQueryModel queryModel);
    
    R queryMerchantEmployees(Long merchantUid);
    
    R selectPromotionMerchantDetail(MerchantPromotionEmployeeDetailQueryModel queryModel);
    
    R queryMerchantByChannelEmployeeUid(Long employeeUid);
    
    R statisticUserV2(Integer type, Long uid, Long beginTime, Long endTime, Integer userType);

    R statisticUserForEmployee(Integer type, Long uid, Long beginTime, Long endTime, Integer userType);
}
