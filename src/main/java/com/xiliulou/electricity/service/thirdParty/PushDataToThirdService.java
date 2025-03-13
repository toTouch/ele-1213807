package com.xiliulou.electricity.service.thirdParty;

/**
 * @author HeYafeng
 * @date 2024/10/12 09:08:33
 */
public interface PushDataToThirdService {
    
    void asyncPushExchangeOrder(String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushRentOrder(String traceId, Integer tenantId, String orderId, Integer orderType);
    
    void asyncPushCabinet(String traceId, Integer tenantId, Long eid, String operateType);
    
    void asyncPushCabinetStatus(String traceId, Integer tenantId, Long eid, Integer delayLevel, String operateType);
    
    void asyncPushUserMemberCardOrder(String traceId, Integer tenantId, Long uid, String mtOrderId, Integer orderType);
}
