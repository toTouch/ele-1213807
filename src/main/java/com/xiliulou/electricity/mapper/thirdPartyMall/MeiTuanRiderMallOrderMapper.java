package com.xiliulou.electricity.mapper.thirdPartyMall;

import com.xiliulou.electricity.entity.meituan.MeiTuanRiderMallOrder;
import com.xiliulou.electricity.query.thirdPartyMall.OrderQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单
 * @date 2024/8/28 21:30:57
 */
public interface MeiTuanRiderMallOrderMapper {
    
    MeiTuanRiderMallOrder selectByMtOrderId(@Param("orderId") String orderId, @Param("phone") String phone, @Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    List<MeiTuanRiderMallOrder> selectByPhone(OrderQuery query);
    
    Integer update(MeiTuanRiderMallOrder meiTuanRiderMallOrder);
    
    MeiTuanRiderMallOrder selectByOrderId(@Param("orderId") String orderId, @Param("uid") Long uid, @Param("tenantId") Integer tenantId);
    
    Integer updateStatusByOrderId(MeiTuanRiderMallOrder meiTuanRiderMallOrder);
    
    Integer updatePhone(@Param("oldPhone") String oldPhone, @Param("newPhone") String newPhone, @Param("tenantId") Integer tenantId);
}
