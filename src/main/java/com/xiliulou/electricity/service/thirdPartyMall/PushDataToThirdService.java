package com.xiliulou.electricity.service.thirdPartyMall;

/**
 * @author HeYafeng
 * @date 2024/10/12 09:08:33
 */
public interface PushDataToThirdService {
    
    void asyncPushExchangeToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushUserAndBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushUserToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushBatteryToThird(Integer mallType, String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushCabinetToThird(Integer mallType, String traceId, Integer tenantId, Long eid);
    
    void asyncPushUserMemberCardToThird(Integer mallType, String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType);
}
