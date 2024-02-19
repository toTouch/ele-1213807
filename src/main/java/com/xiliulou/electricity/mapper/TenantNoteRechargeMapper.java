package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.TenantNote;
import com.xiliulou.electricity.entity.TenantNoteRecharge;
import com.xiliulou.electricity.queryModel.tenantNote.TenantNoteRechargeQueryModel;
import com.xiliulou.electricity.request.tenantNote.TenantRechargePageRequest;
import com.xiliulou.electricity.vo.TenantNoteRechargeVo;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2023/12/28 10:54
 * @desc
 */
public interface TenantNoteRechargeMapper extends BaseMapper<TenantNoteRecharge> {
    int insertOne(TenantNoteRecharge recharge);
    
    Integer countTotal(TenantNoteRechargeQueryModel request);
    
    List<TenantNoteRecharge> selectListByPage(TenantNoteRechargeQueryModel queryModel);
}
