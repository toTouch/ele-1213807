package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.RentBatteryOrder;

public interface ReturnBatterySuccessHandlerService {
    
    void checkReturnBatteryDoor(RentBatteryOrder rentBatteryOrder);
}
