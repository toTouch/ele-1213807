package com.xiliulou.electricity.service.thirdParty;

/**
 * @author HeYafeng
 * @date 2024/10/12 09:08:33
 */
public interface PushDataToThirdService {
    
    void asyncPushExchangeAndUserAndBatteryToThird(String traceId, Integer tenantId, String orderId, Integer orderType, Long uid);
    
    void asyncPushUserAndBatteryToThird(String traceId, Integer tenantId, String orderId, Integer orderType, Long uid);
    
    void asyncPushExchangeToThird(String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushUserToThird(String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushBatteryToThird(String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushCabinetToThird(String traceId, Integer tenantId, Long eid);
    
    void asyncPushCabinetStatusToThird(String traceId, Integer tenantId, Long eid, Integer delayLevel);
    
    void asyncPushUserMemberCardToThird(String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType);
}
