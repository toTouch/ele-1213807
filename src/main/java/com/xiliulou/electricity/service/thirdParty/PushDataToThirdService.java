package com.xiliulou.electricity.service.thirdParty;

/**
 * @author HeYafeng
 * @date 2024/10/12 09:08:33
 */
public interface PushDataToThirdService {
    
    void asyncPushExchangeAndUserAndBatteryToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid);
    
    void asyncPushUserAndBatteryToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid);
    
    void asyncPushExchangeToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushUserToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushBatteryToThird(Integer channel, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushCabinetToThird(Integer channel, String traceId, Integer tenantId, Long eid);
    
    void asyncPushCabinetStatusToThird(Integer channel, String traceId, Integer tenantId, Long eid, Integer delayLevel);
    
    void asyncPushUserMemberCardToThird(Integer channel, String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType);
}
