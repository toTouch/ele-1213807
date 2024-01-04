package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleUserOperateHistory;
import com.xiliulou.electricity.entity.EleUserOperateRecord;

/**
 * Description: EleUserOperateHistoryService
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2023/12/28 11:45
 */
public interface EleUserOperateHistoryService {
    
    void insertOne(EleUserOperateHistory EleUserOperateHistory);
    
    void asyncHandleEleUserOperateHistory(EleUserOperateHistory eleUserOperateHistory);
    
    void asyncHandleUpdateUserPhone(Integer tenantId, Long uid, String newPhone);
}
