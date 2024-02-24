package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.TenantNote;
import org.apache.ibatis.annotations.Param;

/**
 * @author maxiaodong
 * @date 2023/12/27 20:30
 * @desc
 */
public interface TenantNoteMapper extends BaseMapper<TenantNote> {
    TenantNote selectByTenantId(@Param("tenantId") Integer tenantId);
    
    int reduceNoteNum(TenantNote tenantNote);
    
    int insertOne(TenantNote addNote);
    
    int addNoteNum(TenantNote addNote);
}
