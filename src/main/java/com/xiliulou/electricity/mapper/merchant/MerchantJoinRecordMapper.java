package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantJoinRecord;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 18:25:02
 */
public interface MerchantJoinRecordMapper {
    
    Integer insertOne(MerchantJoinRecord record);
    
    Integer existsInProtectionTimeByJoinUid(Long joinUid);
}
