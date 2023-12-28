package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.TenantNote;
import com.xiliulou.electricity.request.tenantNote.TenantRechargeRequest;
import org.apache.commons.lang3.tuple.Triple;

/**
 * @author maxiaodong
 * @date 2023/12/27 20:31
 * @desc
 */
public interface TenantNoteService {
    TenantNote queryFromCacheByTenantId(Integer tenantId);
    
    void updateNoteNumById(TenantNote tenantNote);
    
    Triple<Boolean, String, Object> recharge(TenantRechargeRequest rechargeRequest, Long uid);
    
    void deleteCache(Integer tenantId);
}
