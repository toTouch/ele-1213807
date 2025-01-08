package com.xiliulou.electricity.service.exchange.success;

import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.service.exchange.AbstractOrderHandler;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;


/**
 * @author renhang
 */
@Slf4j
public abstract class AbstractSuccessOrderHandler extends AbstractOrderHandler implements OrderProcessingStrategy {

    @Override
    public Pair<Boolean, ExchangeUserSelectVO> processOrder(ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo, RentBatteryOrder rentBatteryOrder, ElectricityCabinetOrder lastOrder) {
        return lastExchangeHandler(lastOrder, cabinet, electricityBattery, rentBatteryOrder, userInfo);
    }

    public abstract Pair<Boolean, ExchangeUserSelectVO> lastExchangeHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, RentBatteryOrder rentBatteryOrder, UserInfo userInfo);


}
