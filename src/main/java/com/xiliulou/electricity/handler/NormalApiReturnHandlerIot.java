package com.xiliulou.electricity.handler;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ApiReturnOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import com.xiliulou.electricity.service.ApiReturnOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ThirdCallBackUrlService;
import com.xiliulou.electricity.service.retrofilt.api.ApiRentOrderRetrofitService;
import com.xiliulou.electricity.service.retrofilt.api.ApiReturnOrderRetrofitService;
import com.xiliulou.electricity.service.retrofilt.api.RetrofitThirdApiService;
import com.xiliulou.electricity.web.query.ApiRentOrderCallQuery;
import com.xiliulou.electricity.web.query.ApiReturnOrderCallQuery;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Objects;
import java.util.Optional;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalApiReturnHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    ApiReturnOrderService apiReturnOrderService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    RetrofitThirdApiService retrofitThirdApiService;
    @Autowired
    ThirdCallBackUrlService thirdCallBackUrlService;


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
        ApiReturnBatteryOrderRsp apiReturnBatteryOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ApiReturnBatteryOrderRsp.class);
        if (Objects.isNull(apiReturnBatteryOrderRsp)) {
            log.error("API RETURN ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return false;
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("API RETURN ORDER ERROR! electricityCabinet is null! requestId={} p={},d={}", receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }

        ApiReturnOrder apiReturnOrder = apiReturnOrderService.queryByOrderId(apiReturnBatteryOrderRsp.getOrderId(), electricityCabinet.getTenantId());
        if (Objects.isNull(apiReturnOrder)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            log.error("API RETURN ORDER ERROR! rentOrder not found !requestId={},orderId={}", receiverMessage.getSessionId(), apiReturnOrder.getOrderId());
            return false;
        }

        if (apiReturnBatteryOrderRsp.getIsException()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
        }

        if (apiReturnOrder.getOrderSeq() > apiReturnBatteryOrderRsp.getOrderSeq()) {
            log.error("API RETURN ORDER ERROR! rsp order seq is lower order! requestId={},orderId={}", receiverMessage.getSessionId(), apiReturnBatteryOrderRsp.getOrderId());
            return false;
        }

        apiReturnOrder.setOrderSeq(apiReturnBatteryOrderRsp.getOrderSeq());
        apiReturnOrder.setUpdateTime(System.currentTimeMillis());
        apiReturnOrder.setStatus(apiReturnBatteryOrderRsp.getOrderStatus());

        if (apiReturnOrder.getStatus().equalsIgnoreCase(ApiReturnOrder.STATUS_RETURN_BATTERY_CHECK_SUCCESS)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());

            if (StrUtil.isNotEmpty(apiReturnOrder.getBatterySn()) && !apiReturnBatteryOrderRsp.getRentBatteryName().equalsIgnoreCase(apiReturnOrder.getBatterySn())) {
                log.warn("API RENT ORDER WARN! order's origin battery not match now battery sn! requestId={} order's battery ={} rentBattery={}", receiverMessage.getSessionId(), apiReturnOrder.getBatterySn(), apiReturnBatteryOrderRsp.getRentBatteryName());
            }

            apiReturnOrder.setBatterySn(apiReturnBatteryOrderRsp.getRentBatteryName());
            Optional.ofNullable(electricityBatteryService.queryBySn(apiReturnBatteryOrderRsp.getRentBatteryName())).map(e -> {
                e.setUpdateTime(System.currentTimeMillis());
                e.setStatus(ElectricityBattery.STOCK_STATUS);
                electricityBatteryService.update(e);
                return e;
            });

        }
        apiReturnOrderService.update(apiReturnOrder);
        // TODO: 2021/11/10 调用api


        ApiReturnOrderCallQuery apiReturnOrderCallQuery = new ApiReturnOrderCallQuery();
        apiReturnOrderCallQuery.setIsException(apiReturnBatteryOrderRsp.getIsException());
        apiReturnOrderCallQuery.setMsg(apiReturnBatteryOrderRsp.getMsg());
        apiReturnOrderCallQuery.setOrderId(apiReturnBatteryOrderRsp.getOrderId());
        apiReturnOrderCallQuery.setDeviceName(receiverMessage.getDeviceName());
        apiReturnOrderCallQuery.setCellNo(apiReturnOrder.getCellNo());
        apiReturnOrderCallQuery.setProductKey(receiverMessage.getProductKey());
        apiReturnOrderCallQuery.setStatus(apiReturnBatteryOrderRsp.getOrderStatus());
        apiReturnOrderCallQuery.setTimestamp(System.currentTimeMillis());
        apiReturnOrderCallQuery.setRequestId(receiverMessage.getSessionId());


        Call<R> rCall = retrofitThirdApiService.getRetrofitService(ApiReturnOrderRetrofitService.class).apiCall(apiReturnOrderCallQuery, apiReturnOrder.getTenantId(), ThirdCallBackUrl.RETURN_URL);
        rCall.enqueue(new Callback<R>() {
            @Override
            public void onResponse(Call<R> call, Response<R> response) {
                log.info("ELE API INFO! sessionId={} call rsp={}", receiverMessage.getSessionId(), response.body());
            }

            @Override
            public void onFailure(Call<R> call, Throwable throwable) {
                log.error("ELE API ERROR! sessionId={} call error!", receiverMessage.getSessionId(), throwable);
            }
        });
        return true;
    }

}

@Data
class ApiReturnBatteryOrderRsp {
    public String orderId;
    /**
     * 正确或者错误信息，当isProcessFail使用该msg
     */
    public String msg;
    /**
     * 订单状态
     */
    public String orderStatus;
    private Double orderSeq;
    private Boolean isException;
    private Long reportTime;
    private String rentBatteryName;
}
