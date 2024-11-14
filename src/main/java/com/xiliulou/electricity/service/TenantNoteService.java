package com.xiliulou.electricity.service;


import com.xiliulou.electricity.entity.FailureAlarm;
import com.xiliulou.electricity.entity.TenantNote;
import com.xiliulou.electricity.request.tenantNote.TenantRechargeRequest;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2023/12/27 20:31
 * @desc
 */
public interface TenantNoteService {
    TenantNote queryFromCacheByTenantId(Integer tenantId);
    
    void updateNoteNumById(TenantNote tenantNote);
    
    Triple<Boolean, String, Object> recharge(TenantRechargeRequest rechargeRequest, Long uid);
    
    TenantNote queryFromDbByTenantId(Integer tenantId);
    
    void deleteCache(Integer tenantId);
    
    int reduceNoteNumById(TenantNote tenantNote);
    
    List<TenantNote> listByTenantIdList(List<Integer> tenantIdList);
}
