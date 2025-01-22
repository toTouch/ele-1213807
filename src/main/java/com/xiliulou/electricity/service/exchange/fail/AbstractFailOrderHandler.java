package com.xiliulou.electricity.service.exchange.fail;


import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.service.exchange.AbstractOrderHandler;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;


/**
 * @Description: AbstractFailOrderHandler
 * @Author: renhang
 * @Date: 2025/01/07
 */

@Slf4j
public abstract class AbstractFailOrderHandler extends AbstractOrderHandler implements OrderStatusStrategy {


    @Override
    public Pair<Boolean, ExchangeUserSelectVO> process(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo, Integer code, Integer secondFlexibleRenewal) {
        return lastExchangeFailHandler(lastOrder, electricityBattery, cabinet, userInfo, code, secondFlexibleRenewal);
    }


    /**
     * @param lastOrder             lastOrder
     * @param electricityBattery    electricityBattery
     * @param cabinet               cabinet
     * @param userInfo              userInfo
     * @param code                  code
     * @param secondFlexibleRenewal secondFlexibleRenewal
     * @return: Pair
     */

    abstract Pair<Boolean, ExchangeUserSelectVO> lastExchangeFailHandler(ElectricityCabinetOrder lastOrder, ElectricityBattery electricityBattery, ElectricityCabinet cabinet,
                                                                         UserInfo userInfo, Integer code, Integer secondFlexibleRenewal);


}
