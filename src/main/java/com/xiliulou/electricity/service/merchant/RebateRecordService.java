package com.xiliulou.electricity.service.merchant;

import com.xiliulou.electricity.entity.merchant.RebateRecord;
import com.xiliulou.electricity.request.merchant.RebateRecordRequest;
import com.xiliulou.electricity.vo.merchant.RebateRecordVO;

import java.util.List;

/**
 * (RebateRecord)表服务接口
 *
 * @author zzlong
 * @since 2024-02-20 14:31:51
 */
public interface RebateRecordService {
    
    RebateRecord queryById(Long id);
    
    RebateRecord queryByOrderId(String orderId);
    
    RebateRecord insert(RebateRecord rebateRecord);
    
    Integer updateById(RebateRecord rebateRecord);
    
    Integer deleteById(Long id);
    
    List<RebateRecordVO> listByPage(RebateRecordRequest query);
    
    Integer countByPage(RebateRecordRequest query);
}
