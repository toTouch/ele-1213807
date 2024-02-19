package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.MerchantUserAmount;
import com.xiliulou.electricity.query.merchant.MerchantUserAmountQueryMode;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/18 19:37
 * @desc
 */
public interface MerchantUserAmountService {
    
    Integer save(MerchantUserAmount merchantUserAmount);
    
    List<MerchantUserAmount> queryList(MerchantUserAmountQueryMode joinRecordQueryMode);
}
