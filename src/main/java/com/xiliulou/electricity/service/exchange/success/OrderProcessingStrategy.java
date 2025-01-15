package com.xiliulou.electricity.service.exchange.success;

import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author renhang
 */
public interface OrderProcessingStrategy {

    /**
     * 处理订单
     * @param cabinet cabinet
     * @param electricityBattery electricityBattery
     * @param userInfo userInfo
     * @param rentBatteryOrder rentBatteryOrder
     * @param lastOrder lastOrder
     * @return Pair
     */
    Pair<Boolean, ExchangeUserSelectVO> processOrder(ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo, RentBatteryOrder rentBatteryOrder, ElectricityCabinetOrder lastOrder);
}
