package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleUserOperateHistory;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.query.EleUserOperateHistoryQueryModel;

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
    
    R listEleUserOperateHistory(EleUserOperateHistoryQueryModel queryModel);
}
