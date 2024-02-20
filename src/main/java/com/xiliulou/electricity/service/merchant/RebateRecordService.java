package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;

/**
 * (RebateRecord)表服务接口
 *
 * @author zzlong
 * @since 2024-02-20 14:31:51
 */
public interface RebateRecordService {
    
    RebateRecord queryById(Long id);
    
    RebateRecord insert(RebateRecord rebateRecord);
    
    Integer updateById(RebateRecord rebateRecord);
    
    Integer deleteById(Long id);
    
}
