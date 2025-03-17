package com.xiliulou.electricity.service.thirdParty;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/12 09:08:33
 */
public interface PushDataToThirdService {
    
    void asyncPushExchangeOrder(String traceId, Integer tenantId, String orderId, String orderType);
    
    void asyncPushRentOrder(String traceId, Integer tenantId, String orderId, String orderType);
    
    void asyncPushCabinet(String traceId, Integer tenantId, Long eid, String operateType);
    
    void asyncPushCabinetList(String traceId, Integer tenantId, List<Long> eidList, String operateType);
    
    void asyncPushCabinetStatus(String traceId, Integer tenantId, Long eid, Integer delayLevel, String operateType);
    
    void asyncPushUserMemberCardOrder(String traceId, Integer tenantId, Long uid, String mtOrderId, String orderType);
    
    void asyncPushStore(String traceId, Integer tenantId, Long storeId, String operateType);
    
    void asyncPushBattery(String traceId, Integer tenantId, String sn, String operateType);
    
    void asyncPushBatteryList(String traceId, Integer tenantId, List<String> snList, String operateType);
    
    void asyncPushUserInfo(String traceId, Integer tenantId, Long uid, String operateType);
    
}
