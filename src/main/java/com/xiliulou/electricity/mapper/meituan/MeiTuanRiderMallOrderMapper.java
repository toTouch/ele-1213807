package com.xiliulou.electricity.mapper.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.service.meituan.MeiTuanRiderMallConfigService;
import org.apache.ibatis.annotations.Param;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 21:30:57
 */
public interface MeiTuanRiderMallOrderMapper {
    
    MeiTuanRiderMallOrder selectByOrderIdAndPhone(@Param("orderId") String orderId, @Param("phone") String phone);
    
    Integer insert(MeiTuanRiderMallOrder order);
}
