package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.TenantNoteRecharge;
import com.xiliulou.electricity.request.tenantNote.TenantRechargePageRequest;
import com.xiliulou.electricity.vo.TenantNoteRechargeVo;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2023/12/28 10:57
 * @desc
 */
public interface TenantNoteRechargeService {
    
    int insertOne(TenantNoteRecharge recharge);
    
    Integer countTotal(TenantRechargePageRequest allocateRecordPageRequest);
    
    List<TenantNoteRechargeVo> listByPage(TenantRechargePageRequest request);
}
