package com.xiliulou.electricity.service.exchange.fail;

import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.vo.ExchangeUserSelectVO;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @Description: OrderStatusStrategy
 * @Author: renhang
 * @Date: 2025/01/07
 */

public interface OrderStatusStrategy {


    /**
     * @param lastOrder             lastOrder
     * @param cabinet               cabinet
     * @param electricityBattery    electricityBattery
     * @param userInfo              userInfo
     * @param code                  code
     * @param secondFlexibleRenewal secondFlexibleRenewal
     * @return: Pair
     */

    Pair<Boolean, ExchangeUserSelectVO> process(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, UserInfo userInfo,
                                                Integer code, Integer secondFlexibleRenewal);
}
