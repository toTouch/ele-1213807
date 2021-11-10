package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ApiRentOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.ApiRentOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalApiRentHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ApiRentOrderService apiRentOrderService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Override
    protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
        String sessionId = generateSessionId(hardwareCommandQuery);
        SendHardwareMessage message = SendHardwareMessage.builder()
                .sessionId(sessionId)
                .type(hardwareCommandQuery.getCommand())
                .data(hardwareCommandQuery.getData()).build();
        return Pair.of(message, sessionId);
    }

    @Override
    protected boolean receiveMessageProcess(ReceiverMessage receiverMessage) {
        ApiRentBatteryOrderRsp apiRentBatteryOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ApiRentBatteryOrderRsp.class);
        if (Objects.isNull(apiRentBatteryOrderRsp)) {
            log.error("API RENT ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return false;
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("API RENT ORDER ERROR! electricityCabinet is null! requestId={} p={},d={}", receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }

        ApiRentOrder apiRentOrder = apiRentOrderService.queryByOrderId(apiRentBatteryOrderRsp.getOrderId(), electricityCabinet.getTenantId());
        if (Objects.isNull(apiRentOrder)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            log.error("API RENT ORDER ERROR! rentOrder not found !requestId={},orderId={}", receiverMessage.getSessionId(), apiRentBatteryOrderRsp.getOrderId());
            return false;
        }

        if (apiRentBatteryOrderRsp.getIsException()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
        }

        if (apiRentOrder.getOrderSeq() > apiRentBatteryOrderRsp.getOrderSeq()) {
            log.error("API RENT ORDER ERROR! rsp order seq is lower order! requestId={},orderId={}", receiverMessage.getSessionId(), apiRentBatteryOrderRsp.getOrderId());
            return false;
        }

        apiRentOrder.setOrderSeq(apiRentBatteryOrderRsp.getOrderSeq());
        apiRentOrder.setUpdateTime(System.currentTimeMillis());
        apiRentOrder.setStatus(apiRentBatteryOrderRsp.getOrderStatus());

        //处理取走电池的状态
        if (apiRentOrder.getStatus().equalsIgnoreCase(ApiRentOrder.STATUS_RENT_BATTERY_TAKE_SUCCESS)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());

            if (!apiRentBatteryOrderRsp.getRentBatteryName().equalsIgnoreCase(apiRentOrder.getBatterySn())) {
                log.warn("API RENT ORDER WARN! order's origin battery not match now battery sn! requestId={} order's battery ={} rentBattery={}", receiverMessage.getSessionId(), apiRentOrder.getBatterySn(), apiRentBatteryOrderRsp.getRentBatteryName());
            }

            apiRentOrder.setBatterySn(apiRentBatteryOrderRsp.getRentBatteryName());
            Optional.ofNullable(electricityBatteryService.queryBySn(apiRentBatteryOrderRsp.getRentBatteryName())).map(e -> {
                e.setUpdateTime(System.currentTimeMillis());
                e.setStatus(ElectricityBattery.LEASE_STATUS);
                electricityBatteryService.update(e);
                return e;
            });

        }
        apiRentOrderService.update(apiRentOrder);
        // TODO: 2021/11/10 调用api


        return true;
    }


}

@Data
class ApiRentBatteryOrderRsp {
    private String orderId;
    private String msg;
    private String orderStatus;
    private Double orderSeq;
    private Boolean isException;
    private Long reportTime;
    private String rentBatteryName;
}