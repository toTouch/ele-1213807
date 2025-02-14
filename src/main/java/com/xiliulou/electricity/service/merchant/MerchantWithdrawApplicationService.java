package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.ReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawProcessVO;
import org.apache.commons.lang3.tuple.Triple;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/21 17:46
 */
public interface MerchantWithdrawApplicationService {
    
    /**
     * 申请提现
     * @param merchantWithdrawApplicationRequest
     * @return
     */
    Triple<Boolean, String, Object> saveMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    /**
     * 单条提现记录审批
     * @param reviewWithdrawApplicationRequest
     * @return
     */
    Triple<Boolean, String, Object> reviewMerchantWithdrawApplication(ReviewWithdrawApplicationRequest reviewWithdrawApplicationRequest);
    
    /**
     * 批量提现记录审批
     * @param batchReviewWithdrawApplicationRequest
     * @return
     */
    Triple<Boolean, String, Object> batchReviewMerchantWithdrawApplication(BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest);
    
    Integer removeMerchantWithdrawApplication(Long id);
    
    Integer countMerchantWithdrawApplication(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    List<MerchantWithdrawApplicationVO> queryMerchantWithdrawApplicationList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    MerchantWithdrawApplication queryMerchantWithdrawApplication(Long id);
    
    BigDecimal sumByStatus(Integer tenantId, Integer status, Long uid);
    
    void updateMerchantWithdrawStatus();
    
    List<MerchantWithdrawApplicationVO> selectRecordList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    Integer selectRecordListCount(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);

    Triple<Boolean, String, Object> getMerchantWithdrawProcess(Long uid);
}
