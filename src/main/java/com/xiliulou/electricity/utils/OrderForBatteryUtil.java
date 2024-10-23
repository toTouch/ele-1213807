package com.xiliulou.electricity.utils;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.dto.OrderForBatteryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     */
    public static void save(String orderId, Integer type, String sn) {
        OrderForBatteryDTO orderForBatteryDTO = OrderForBatteryDTO.builder().orderTypeForBattery(type).orderIdForBattery(orderId).build();
        redisService.saveWithHash(String.format(CacheConstant.ORDER_FOR_BATTERY_WITH_BUSINESS_STATUS_LEASE, sn), orderForBatteryDTO);
    }
    
    public static void delete(String sn) {
        redisService.delete(String.format(CacheConstant.ORDER_FOR_BATTERY_WITH_BUSINESS_STATUS_LEASE, sn));
    }
}
