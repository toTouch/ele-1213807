package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ApiExchangeOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.ApiExchangeOrderService;
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
public class NormalApiExchangeHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    ApiExchangeOrderService apiExchangeOrderService;

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
        ApiExchangeOrderRsp apiExchangeOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ApiExchangeOrderRsp.class);
        if (Objects.isNull(apiExchangeOrderRsp)) {
            log.error("API EXCHANGE ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return false;
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("API EXCHANGE ORDER ERROR! electricityCabinet is null! requestId={} p={},d={}", receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }

        ApiExchangeOrder apiExchangeOrder = apiExchangeOrderService.queryByOrderId(apiExchangeOrderRsp.getOrderId(), electricityCabinet.getTenantId());
        if (Objects.isNull(apiExchangeOrder)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            log.error("API EXCHANGE ORDER ERROR! rentOrder not found !requestId={},orderId={}", receiverMessage.getSessionId(), apiExchangeOrder.getOrderId());
            return false;
        }

        if (apiExchangeOrderRsp.getIsException()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
        }

        if (apiExchangeOrder.getOrderSeq() > apiExchangeOrderRsp.getOrderSeq()) {
            log.error("API EXCHANGE ORDER ERROR! rsp order seq is lower order! requestId={},orderId={}", receiverMessage.getSessionId(), apiExchangeOrderRsp.getOrderId());
            return false;
        }

        apiExchangeOrder.setOrderSeq(apiExchangeOrderRsp.getOrderSeq());
        apiExchangeOrder.setStatus(apiExchangeOrderRsp.getOrderStatus());
        apiExchangeOrder.setUpdateTime(System.currentTimeMillis());

        //处理取走电池成功
        if (apiExchangeOrder.getStatus().equalsIgnoreCase(ApiExchangeOrder.STATUS_PLACE_BATTERY_CHECK_SUCCESS)) {
            if (StrUtil.isNotEmpty(apiExchangeOrder.getPutBatterySn()) && !apiExchangeOrderRsp.getPlaceBatteryName().equalsIgnoreCase(apiExchangeOrder.getPutBatterySn())) {
                log.warn("API EXCHANGE ORDER WARN! order's origin battery not match now battery sn! requestId={} order's battery ={} rentBattery={}", receiverMessage.getSessionId(), apiExchangeOrder.getPutBatterySn(), apiExchangeOrderRsp.getPlaceBatteryName());
            }

            apiExchangeOrder.setPutBatterySn(apiExchangeOrderRsp.getPlaceBatteryName());
            Optional.ofNullable(electricityBatteryService.queryBySn(apiExchangeOrderRsp.getPlaceBatteryName())).map(e -> {
                e.setUpdateTime(System.currentTimeMillis());
                e.setStatus(ElectricityBattery.STOCK_STATUS);
                electricityBatteryService.update(e);
                return e;
            });
        }

        //处理拿走电池的流程
        if (apiExchangeOrder.getStatus().equalsIgnoreCase(ApiExchangeOrder.STATUS_TAKE_BATTERY_SUCCESS)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            if (StrUtil.isNotEmpty(apiExchangeOrder.getTakeBatterySn()) && !apiExchangeOrderRsp.getTakeBatteryName().equalsIgnoreCase(apiExchangeOrder.getTakeBatterySn())) {
                log.warn("API EXCHANGE ORDER WARN! order's origin battery not match now battery sn! requestId={} order's battery ={} rentBattery={}", receiverMessage.getSessionId(), apiExchangeOrder.getTakeBatterySn(), apiExchangeOrderRsp.getTakeBatteryName());
            }

            apiExchangeOrder.setTakeBatterySn(apiExchangeOrderRsp.getTakeBatteryName());
            Optional.ofNullable(electricityBatteryService.queryBySn(apiExchangeOrder.getTakeBatterySn())).map(e -> {
                e.setUpdateTime(System.currentTimeMillis());
                e.setStatus(ElectricityBattery.LEASE_STATUS);
                electricityBatteryService.update(e);
                return e;
            });
        }

        apiExchangeOrderService.update(apiExchangeOrder);
        // TODO: 2021/11/10 call
        return true;
    }

}

@Data
class ApiExchangeOrderRsp {
    private String orderId;

    //正确或者错误信息，当isProcessFail使用该msg
    private String msg;
    //订单状态
    private String orderStatus;
    //订单状态序号
    private Double orderSeq;
    //是否需要结束订单
    private Boolean isException;
    //创建时间
    private Long reportTime;

    private Integer placeCellNo;

    private String placeBatteryName;

    private String takeBatteryName;

    private Integer takeCellNo;
}