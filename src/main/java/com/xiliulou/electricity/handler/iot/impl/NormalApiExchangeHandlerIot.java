package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ApiExchangeOrder;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ApiExchangeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ThirdCallBackUrlService;
import com.xiliulou.electricity.service.retrofilt.api.ApiExchangeOrderRetrofitService;
import com.xiliulou.electricity.service.retrofilt.api.RetrofitThirdApiService;
import com.xiliulou.electricity.web.query.ApiExchangeOrderCallQuery;
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
@Service(value= ElectricityIotConstant.NORMAL_API_EXCHANGE_HANDLER)
@Slf4j
public class NormalApiExchangeHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Autowired
    ApiExchangeOrderService apiExchangeOrderService;

    @Autowired
    RetrofitThirdApiService retrofitThirdApiService;
    @Autowired
    ThirdCallBackUrlService thirdCallBackUrlService;


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        ApiExchangeOrderRsp apiExchangeOrderRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), ApiExchangeOrderRsp.class);
        if (Objects.isNull(apiExchangeOrderRsp)) {
            log.error("API EXCHANGE ORDER ERROR! originData is null! requestId={}", receiverMessage.getSessionId());
            return ;
        }

        ApiExchangeOrder apiExchangeOrder = apiExchangeOrderService.queryByOrderId(apiExchangeOrderRsp.getOrderId(), electricityCabinet.getTenantId());
        if (Objects.isNull(apiExchangeOrder)) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            log.error("API EXCHANGE ORDER ERROR! rentOrder not found !requestId={},orderId={}", receiverMessage.getSessionId(), apiExchangeOrder.getOrderId());
            return ;
        }

        if (apiExchangeOrderRsp.getIsException()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
        }

        if (apiExchangeOrder.getOrderSeq() > apiExchangeOrderRsp.getOrderSeq()) {
            log.error("API EXCHANGE ORDER ERROR! rsp order seq is lower order! requestId={},orderId={}", receiverMessage.getSessionId(), apiExchangeOrderRsp.getOrderId());
            return ;
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
                e.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
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

        ThirdCallBackUrl thirdCallBackUrl = thirdCallBackUrlService.queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(thirdCallBackUrl) || StrUtil.isEmpty(thirdCallBackUrl.getExchangeUrl())) {
            log.warn("CUPBOARD WARN! tenantId={} hasn't callback!", electricityCabinet.getTenantId());
            return ;
        }

        ApiExchangeOrderCallQuery apiExchangeOrderCallQuery = buildApiExchangeOrderCallQuery( receiverMessage, apiExchangeOrderRsp);

        Call<R> rCall = retrofitThirdApiService.getRetrofitService(ApiExchangeOrderRetrofitService.class).apiCall(apiExchangeOrderCallQuery, apiExchangeOrder.getTenantId(), ThirdCallBackUrl.EXCHANGE_URL);
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


    private ApiExchangeOrderCallQuery buildApiExchangeOrderCallQuery(ReceiverMessage receiverMessage,ApiExchangeOrderRsp apiExchangeOrderRsp){
       return ApiExchangeOrderCallQuery.builder()
                .deviceName(receiverMessage.getDeviceName())
                .productKey(receiverMessage.getProductKey())
                .isException(apiExchangeOrderRsp.getIsException())
                .msg(apiExchangeOrderRsp.getMsg())
                .orderId(apiExchangeOrderRsp.getOrderId())
                .requestId(receiverMessage.getSessionId())
                .returnBatteryName(apiExchangeOrderRsp.getPlaceBatteryName())
                .takeBatteryName(apiExchangeOrderRsp.getTakeBatteryName())
                .placeCellNo(apiExchangeOrderRsp.getPlaceCellNo())
                .takeCellNo(apiExchangeOrderRsp.getTakeCellNo())
                .status(apiExchangeOrderRsp.getOrderStatus())
                .timestamp(System.currentTimeMillis()).build();
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
}


