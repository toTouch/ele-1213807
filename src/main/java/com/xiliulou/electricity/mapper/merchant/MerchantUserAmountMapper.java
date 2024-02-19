package com.xiliulou.electricity.mapper.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.query.merchant.MerchantUserAmountQueryMode;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 19:32
 * @desc
 */
public interface MerchantUserAmountMapper {
    
    Integer insert(MerchantUserAmount merchantUserAmount);
    
    List<MerchantUserAmount> queryList(MerchantUserAmountQueryMode joinRecordQueryMode);
}
