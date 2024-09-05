package com.xiliulou.electricity.mapper.meituan;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.meituan.OrderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 21:30:57
 */
public interface MeiTuanRiderMallOrderMapper {
    
    MeiTuanRiderMallOrder selectByOrderId(@Param("orderId") String orderId, @Param("phone") String phone, @Param("uid") Long uid);
    
    List<MeiTuanRiderMallOrder> selectByUid(OrderQuery query);
    
    Integer update(MeiTuanRiderMallOrder meiTuanRiderMallOrder);
}
