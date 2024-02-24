package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplication;
import com.xiliulou.electricity.request.merchant.BatchReviewWithdrawApplicationRequest;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationVO;
import feign.Param;

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
    
}
