package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.bo.merchant.MerchantWithdrawSendBO;
import com.xiliulou.electricity.bo.wechat.WechatPayParamsDetails;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.ReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
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

    List<MerchantWithdrawSendBO> listAuditSuccess(Integer tenantId, Long size, Long startId, Integer type);

    Triple<Boolean, String, Object> sendTransfer(MerchantWithdrawSendBO merchantWithdrawSendBO, String userThird, WechatPayParamsDetails finalWechatPayParamsDetails, Integer payConfigType);

    List<MerchantWithdrawSendBO> listWithdrawingByMerchantId(Long uid, Long size, Long startId, Long checkTime);

    Integer batchUpdatePayConfigChangeByIdList(List<Long> idList, Integer payConfigWhetherChangeYes);

    Integer updateStateById(Long applicationId, Integer state);

    MerchantWithdrawApplication queryByOrderNo(String orderNo, String batchNo);

    Integer updateById(MerchantWithdrawApplication merchantWithdrawApplicationUpdate);
}
