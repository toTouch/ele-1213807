package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantWithdrawApplicationRecord;
import com.xiliulou.electricity.request.merchant.MerchantWithdrawApplicationRecordRequest;
import com.xiliulou.electricity.vo.merchant.MerchantWithdrawApplicationRecordVO;
import feign.Param;

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
    
    List<MerchantWithdrawApplicationRecordVO> queryList(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    List<MerchantWithdrawApplicationRecordVO> selectListByCondition(MerchantWithdrawApplicationRecordRequest merchantWithdrawApplicationRecordRequest);
    
    Integer countByCondition(MerchantWithdrawApplicationRecord merchantWithdrawApplicationRecord);
    
    Integer removeById(@Param("id") Long id);
    
}
