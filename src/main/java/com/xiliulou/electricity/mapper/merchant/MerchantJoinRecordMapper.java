package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;
import com.xiliulou.electricity.query.merchant.MerchantJoinRecordQueryMode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 18:25:02
 */
public interface MerchantJoinRecordMapper {
    
    Integer insertOne(MerchantJoinRecord record);
    
    Integer existsInProtectionTimeByJoinUid(Long joinUid);
    
    MerchantJoinRecord selectByMerchantIdAndJoinUid(@Param("merchantId") Long merchantId, @Param("joinUid") Long joinUid);
    
    Integer existsIfExpired(@Param("merchantId") Long merchantId, @Param("joinUid") Long joinUid);
    
    Integer updateStatus(@Param("merchantId") Long merchantId, @Param("joinUid") Long joinUid, @Param("status") Integer status);
    
    Integer updateProtectionExpired(@Param("protectionJoinRecord") MerchantJoinRecord protectionJoinRecord);
    
    Integer updateExpired(@Param("merchantJoinRecord")MerchantJoinRecord merchantJoinRecord);
    
    List<MerchantJoinRecord> selectList(MerchantJoinRecordQueryMode joinRecordQueryMode);
    
    List<MerchantJoinRecord> selectListByMerchantIdAndStatus(@Param("merchantId")Long merchantId, @Param("status")Integer status);
    
    Integer countTotal(MerchantJoinRecordQueryMode queryMode);
    
    List<MerchantJoinRecord> selectListByPage(MerchantJoinRecordQueryMode queryMode);
    
    Integer updateById(MerchantJoinRecord record);
}
