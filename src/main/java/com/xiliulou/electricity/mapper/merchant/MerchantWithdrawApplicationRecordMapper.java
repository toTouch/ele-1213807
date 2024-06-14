package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.bo.merchant.MerchantWithdrawApplicationRecordBO;
import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRecordRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/24 14:06
 */
public interface MerchantWithdrawApplicationRecordMapper {
    
    Integer insertOne(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    Integer updateOne(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    Integer batchInsert(List<MerchantWithdrawApplicationRecord> merchantWithdrawApplicationRecordList);
    
    MerchantWithdrawApplicationRecordVO selectById(@Param("id") Long id);
    
    MerchantWithdrawApplicationRecord selectByOrderNo(@Param("orderNo") String orderNo, @Param("tenantId") Integer tenantId);
    
    List<MerchantWithdrawApplicationRecordVO> queryList(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    List<MerchantWithdrawApplicationRecordVO> selectListByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    Integer countByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    Integer removeById(@Param("id") Long id);
    
    /**
     * 根据batchNo, tenant id批量更新提现明细记录状态
     * @param status
     * @param updateTime
     * @param batchNo
     * @param tenantId
     * @return
     */
    Integer updateApplicationRecordStatusByBatchNo(@Param("status") Integer status, @Param("updateTime") Long updateTime, @Param("batchNo") String batchNo, @Param("tenantId") Integer tenantId);
    
    Integer updateApplicationRecordStatus(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    List<MerchantWithdrawApplicationRecordBO> selectListByBatchNo(@Param("batchNo") String batchNo, @Param("tenantId") Integer tenantId);
    
}
