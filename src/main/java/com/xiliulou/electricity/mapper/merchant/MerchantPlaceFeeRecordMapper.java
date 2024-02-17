package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeRecordQueryModel;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:21
 * @desc
 */
public interface MerchantPlaceFeeRecordMapper {
    
    Integer countTotal(MerchantPlaceFeeRecordQueryModel merchantPlaceFeeQueryModel);
    
    List<MerchantPlaceFeeRecord> selectListByPage(MerchantPlaceFeeRecordQueryModel merchantPlaceFeeQueryModel);
    
    Integer insert(MerchantPlaceFeeRecord merchantPlaceFeeRecord);
}
