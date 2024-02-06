package com.xiliulou.electricity.service.merchant;

import com.xiliulou.core.web.R;

/**
 * @author HeYafeng
 * @description 参与记录
 * @date 2024/2/6 17:53:17
 */
public interface MerchantJoinRecordService {
    
    R joinScanCode(String code);
    
    /**
     * 参与人是否存在保护期内的记录
     */
    Integer existsInProtectionTimeByJoinUid(Long joinUid);
}
