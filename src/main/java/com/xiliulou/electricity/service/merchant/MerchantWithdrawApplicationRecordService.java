package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.bo.merchant.MerchantWithdrawApplicationRecordBO;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRecordRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationRecordVO;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:34
 */
public interface MerchantWithdrawApplicationRecordService {
    
    Integer insertOne(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    Integer batchInsert(List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecords);
    
    Integer updateOne(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    Integer removeById(Long id);
    
    Integer countByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    List<MerchantWithdrawApplicationRecordVO> selectListByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    MerchantWithdrawApplicationRecordVO selectById(Long id);
    
    MerchantWithdrawApplicationRecord selectByOrderNo(String orderNo, Integer tenantId);
    
    Integer updateApplicationRecordStatusByBatchNo(Integer status, String batchNo, Integer tenantId);
    
    List<MerchantWithdrawApplicationRecordBO> selectListByBatchNo(String batchNo, Integer tenantId);
    
    Integer updateMerchantWithdrawRecordStatus(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    List<MerchantWithdrawApplicationRecordVO> selectWithdrawRecordList(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    Integer selectWithdrawRecordListCount(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);

    Integer updateById(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecordUpdate);
}
