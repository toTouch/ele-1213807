package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.request.merchant.MerchantSaveRequest;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:09
 * @desc
 */
public interface MerchantService {
    
    Triple<Boolean, String, Object> save(MerchantSaveRequest merchantSaveRequest, Long uid);
}
