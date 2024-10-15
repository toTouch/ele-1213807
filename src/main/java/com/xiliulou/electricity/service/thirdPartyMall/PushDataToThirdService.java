package com.xiliulou.electricity.service.thirdPartyMall;

/**
 * @author HeYafeng
 * @date 2024/10/12 09:08:33
 */
public interface PushDataToThirdService {
    
    void asyncPushExchangeAndUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid);
    
    void asyncPushUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType, Long uid);
    
    void asyncPushExchangeToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushUserToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushCabinetToThird(Integer mallType, String traceId, Integer tenantId, Long eid);
    
    void asyncPushCabinetStatusToThird(Integer mallType, String traceId, Integer tenantId, Long eid, Integer delayLevel);
    
    void asyncPushUserMemberCardToThird(Integer mallType, String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType);
}
