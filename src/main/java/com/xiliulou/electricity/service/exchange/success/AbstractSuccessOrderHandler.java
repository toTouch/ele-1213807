package com.xiliulou.electricity.service.exchange.success;


import com.xiliulou.electricity.constant.LessScanConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.LastOrderTypeEnum;
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
        ExchangeUserSelectVO vo = new ExchangeUserSelectVO();
        vo.setIsEnterMoreExchange(LessScanConstant.ENTER_MORE_EXCHANGE).setLastExchangeIsSuccess(LessScanConstant.LAST_EXCHANGE_SUCCESS).setCabinetName(cabinet.getName())
                .setLastOrderType(LastOrderTypeEnum.LAST_RENT_ORDER.getCode());

        if (!isSatisfySelfOpenCondition(rentBatteryOrder.getOrderId(), rentBatteryOrder.getElectricityCabinetId(), rentBatteryOrder.getUpdateTime(),
                rentBatteryOrder.getCellNo())) {
            vo.setIsSatisfySelfOpen(LessScanConstant.NOT_SATISFY_SELF_OPEN);
            log.warn("OrderV3 WARN! LastExchangeSuccessHandler RentOrderCell is not SatisfySelfOpen, orderId is{}", rentBatteryOrder.getOrderId());
            return Pair.of(true, vo);
        }
        return lastExchangeHandler(lastOrder, cabinet, electricityBattery, rentBatteryOrder, userInfo, vo);
    }

    public abstract Pair<Boolean, ExchangeUserSelectVO> lastExchangeHandler(ElectricityCabinetOrder lastOrder, ElectricityCabinet cabinet, ElectricityBattery electricityBattery, RentBatteryOrder rentBatteryOrder, UserInfo userInfo, ExchangeUserSelectVO vo);


}
