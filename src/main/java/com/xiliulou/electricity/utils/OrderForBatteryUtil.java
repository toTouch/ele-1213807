package com.xiliulou.electricity.utils;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.OrderForBatteryConstants;
import com.xiliulou.electricity.dto.OrderForBatteryDTO;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.RentBatteryOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/10/15 18:27
 */
@Component
public class OrderForBatteryUtil {
    
    private static RedisService redisService;
    
    @Autowired
    public OrderForBatteryUtil(RedisService redisService) {
        OrderForBatteryUtil.redisService = redisService;
    }
    
    /**
     * 保存电池改为租借状态时对应的换电订单或租电订单
     *
     * @param cabinetOrder     换电订单
     * @param rentBatteryOrder 租电订单
     */
    public static void save(ElectricityCabinetOrder cabinetOrder, RentBatteryOrder rentBatteryOrder) {
        Integer type;
        String orderId;
        String sn;
        if (Objects.nonNull(cabinetOrder)) {
            type = OrderForBatteryConstants.TYPE_ELECTRICITY_CABINET_ORDER;
            orderId = cabinetOrder.getOrderId();
            sn = cabinetOrder.getNewElectricityBatterySn();
        } else {
            type = OrderForBatteryConstants.TYPE_RENT_BATTERY_ORDER;
            orderId = rentBatteryOrder.getOrderId();
            sn = rentBatteryOrder.getElectricityBatterySn();
        }
        
        OrderForBatteryDTO orderForBatteryDTO = OrderForBatteryDTO.builder().orderType(type).orderId(orderId).build();
        redisService.saveWithHash(String.format(CacheConstant.ORDER_FOR_BATTERY_WITH_BUSINESS_STATUS_LEASE, sn), orderForBatteryDTO);
    }
    
    public static void delete(String sn) {
        redisService.delete(String.format(CacheConstant.ORDER_FOR_BATTERY_WITH_BUSINESS_STATUS_LEASE, sn));
    }
}
