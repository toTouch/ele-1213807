package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantInviterModifyRecord;
import com.xiliulou.electricity.query.merchant.MerchantInviterModifyRecordQueryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 修改邀请人记录表
 * @date 2024/3/28 09:44:24
 */
public interface MerchantInviterModifyRecordMapper {
    
    Integer insertOne(MerchantInviterModifyRecord record);
    
    List<MerchantInviterModifyRecord> selectPage(MerchantInviterModifyRecordQueryModel queryModel);
    
    Integer countTotal(MerchantInviterModifyRecordQueryModel queryModel);
    
    Integer existsModifyRecordByUid(@Param("uid") Long uid);
}
