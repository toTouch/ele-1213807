package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import feign.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/20 13:32
 */
public interface MerchantWithdrawApplicationMapper {
    
    Integer insertOne(MerchantWithdrawApplication merchantWithdrawApplication);
    
    Integer updateOne(MerchantWithdrawApplication merchantWithdrawApplication);
    
    Integer updateByIds(BatchReviewWithdrawApplicationRequest batchReviewWithdrawApplicationRequest);
    
    List<MerchantWithdrawApplicationVO> queryList(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    Integer countByCondition(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    List<MerchantWithdrawApplicationVO> selectListByCondition(MerchantWithdrawApplicationRequest merchantWithdrawApplicationRequest);
    
    List<MerchantWithdrawApplication> selectListByIds(@Param("ids") List<Long> ids, @Param("tenantId") Long tenantId);
  
    Integer removeById(@Param("id") Long id);
    
    MerchantWithdrawApplication selectById(@Param("id") Long id);
    
    BigDecimal sumByStatus(@Param("tenantId") Integer tenantId,@Param("status") Integer status,@Param("uid") Long uid);
    
    List<MerchantWithdrawApplication> selectListForWithdrawInProgress(@Param("checkTime") Long checkTime);
    
    List<MerchantWithdrawApplication> selectListByBatchNo(@Param("batchNo") String batchNo, @Param("tenantId") Integer tenantId);
    
    Integer updateMerchantWithdrawStatusByBatchNo(@Param("status") Integer status, @Param("updateTime") Long updateTime, @Param("batchNo") String batchNo, @Param("tenantId") Integer tenantId);
    
}
