package com.xiliulou.electricity.service;

public interface ShippingManagerService {
    
    void uploadShippingInfo(Long uid, String phone, String orderNo, Integer tenantId);
    
}
