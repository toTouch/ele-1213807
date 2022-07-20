package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ApiReturnOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ApiReturnOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ThirdCallBackUrlService;
import com.xiliulou.electricity.service.retrofilt.api.ApiReturnOrderRetrofitService;
import com.xiliulou.electricity.service.retrofilt.api.RetrofitThirdApiService;
import com.xiliulou.electricity.web.query.ApiReturnOrderCallQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Service(value= ElectricityIotConstant.NORMAL_API_RETURN_HANDLER)
@Slf4j
public class NormalApiReturnHandlerIot extends AbstractElectricityIotHandler {
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
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        ApiReturnBatteryOrderRsp apiReturnBatteryOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ApiReturnBatteryOrderRsp.class);
        if (Objects.isNull(apiReturnBatteryOrderRsp)) {
            log.error("API RETURN ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return ;
        }

        ApiReturnOrder apiReturnOrder = apiReturnOrderService.queryByOrderId(apiReturnBatteryOrderRsp.getOrderId(), electricityCabinet.getTenantId());
        if (Objects.isNull(apiReturnOrder)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            log.error("API RETURN ORDER ERROR! rentOrder not found !requestId={},orderId={}", receiverMessage.getSessionId(), apiReturnOrder.getOrderId());
            return ;
        }

        if (apiReturnBatteryOrderRsp.getIsException()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
        }

        if (apiReturnOrder.getOrderSeq() > apiReturnBatteryOrderRsp.getOrderSeq()) {
            log.error("API RETURN ORDER ERROR! rsp order seq is lower order! requestId={},orderId={}", receiverMessage.getSessionId(), apiReturnBatteryOrderRsp.getOrderId());
            return ;
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
                e.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
                electricityBatteryService.update(e);
                return e;
            });

        }
        apiReturnOrderService.update(apiReturnOrder);

        ThirdCallBackUrl thirdCallBackUrl = thirdCallBackUrlService.queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(thirdCallBackUrl) || StrUtil.isEmpty(thirdCallBackUrl.getReturnUrl())) {
            log.warn("CUPBOARD WARN! tenantId={} hasn't callback!", electricityCabinet.getTenantId());
            return ;
        }


        ApiReturnOrderCallQuery apiReturnOrderCallQuery = buildApiReturnOrderCallQuery( apiReturnBatteryOrderRsp, apiReturnOrder, receiverMessage);

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
    }

    private ApiReturnOrderCallQuery buildApiReturnOrderCallQuery(ApiReturnBatteryOrderRsp apiReturnBatteryOrderRsp,ApiReturnOrder apiReturnOrder,ReceiverMessage receiverMessage){

        return ApiReturnOrderCallQuery.builder()
                .isException(apiReturnBatteryOrderRsp.getIsException())
                .msg(apiReturnBatteryOrderRsp.getMsg())
                .orderId(apiReturnBatteryOrderRsp.getOrderId())
                .deviceName(receiverMessage.getDeviceName())
                .cellNo(apiReturnOrder.getCellNo())
                .productKey(receiverMessage.getProductKey())
                .status(apiReturnBatteryOrderRsp.getOrderStatus())
                .timestamp(System.currentTimeMillis())
                .requestId(receiverMessage.getSessionId()).build();
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
}


