package com.xiliulou.electricity.handler.eleapi.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.EleApiConstant;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ApiExchangeOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.query.api.ExchangeQuery;
import com.xiliulou.electricity.service.ApiExchangeOrderService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.api.ApiOrderVo;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2021/11/9 9:31 上午
 */
@Service(value = EleApiConstant.EXCHANGE_ORDER)
@Slf4j
public class EleExchangeOrderHandler implements EleApiHandler {
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    @Autowired
    RedisService redisService;

    @Autowired
    ApiExchangeOrderService apiExchangeOrderService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Override
    public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {
        ExchangeQuery exchangeQuery = JsonUtil.fromJson(apiRequestQuery.getData(), ExchangeQuery.class);

        if (StrUtil.isEmpty(exchangeQuery.getOrderId())) {
            log.error("ELE EXCHANGE ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "orderId不存在");
        }

        if (StrUtil.isNotEmpty(exchangeQuery.getType()) && BatteryConstant.existsBatteryType(exchangeQuery.getType())) {
            log.error("ELE EXCHANGE ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "type类型不合法");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(exchangeQuery.getProductKey(), exchangeQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet) || !TenantContextHolder.getTenantId().equals(electricityCabinet.getTenantId())) {
            log.error("ELE EXCHANGE ORDER ERROR! not found eleCabinet! requestId={},p={},d={}", apiRequestQuery.getRequestId(), exchangeQuery.getProductKey(), exchangeQuery.getDeviceName());
            return Triple.of(false, "API.00002", "柜机不存在");
        }

        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELE EXCHANGE ORDER ERROR!  electricityCabinet is offline ！pd={}", electricityCabinet.getDeviceName());
            return Triple.of(false, "API.00002", "柜机不在线！");
        }


        if (StrUtil.isNotEmpty(exchangeQuery.getReturnBatteryName()) && Objects.isNull(electricityBatteryService.queryBySn(exchangeQuery.getReturnBatteryName()))) {
            log.error("ELE EXCHANGE ORDER ERROR! returnBattery's sn not found! requestId={},batteryName={}", apiRequestQuery.getRequestId(), exchangeQuery.getReturnBatteryName());
            return Triple.of(false, "API.10003", "将要放入的电池不存在系统");
        }

        ApiExchangeOrder apiExchangeOrder = apiExchangeOrderService.queryByOrderId(exchangeQuery.getOrderId(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(apiExchangeOrder)) {
            log.error("ELE EXCHANGE ORDER ERROR! order_id is repeat! requestId={},orderId={}", apiRequestQuery.getRequestId(), exchangeQuery.getOrderId());
            return Triple.of(false, "API.10002", "订单号重复！");
        }

        boolean result = redisService.setNx(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 3 * 60 * 1000L, false);
        if (!result) {
            log.error("ELE EXCHANGE ORDER ERROR! cabinet is busy! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1006", "柜机在使用中，请勿重复下单！");
        }

        Pair<Boolean, ElectricityCabinetBox> usableDistributeCell = electricityCabinetService.findUsableBatteryCellNo(electricityCabinet.getId(), exchangeQuery.getType(), electricityCabinet.getFullyCharged());
        if (!usableDistributeCell.getLeft()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            return Triple.of(false, "API.10001", "没有可以换电的电池");
        }

        Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNo(electricityCabinet.getId());
        if (!usableEmptyCellNo.getLeft()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            return Triple.of(false, "API.10001", "没有空仓");
        }

        ElectricityCabinetBox box = usableDistributeCell.getRight();

        ApiExchangeOrder order = ApiExchangeOrder.builder()
                .createTime(System.currentTimeMillis())
                .batteryType(exchangeQuery.getType())
                .orderId(exchangeQuery.getOrderId())
                .eid(electricityCabinet.getId())
                .status(ApiExchangeOrder.STATUS_INIT)
                .orderSeq(0.0)
                .tenantId(TenantContextHolder.getTenantId())
                .updateTime(System.currentTimeMillis())
                .putBatterySn(exchangeQuery.getReturnBatteryName())
                .putCellNo(usableEmptyCellNo.getRight())
                .takeCellNo(Integer.parseInt(box.getCellNo()))
                .build();
        apiExchangeOrderService.insert(order);

        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("placCellNo", order.getPutCellNo());
        dataMap.put("orderId", order.getOrderId());
        dataMap.put("takeCellNo", order.getTakeCellNo());
        dataMap.put("placeBatteryName", order.getPutBatterySn());
        dataMap.put("placeIsModelType", exchangeQuery.getType() != null);
        dataMap.put("placeMultiBatteryModelName", exchangeQuery.getReturnBatteryName());


        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(apiRequestQuery.getRequestId())
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.API_EXCHANGE_ORDER).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("ELE EXCHANGE ORDER ERROR! send command error! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "API.00003", sendResult.getRight());
        }

        return Triple.of(true, null, new ApiOrderVo(order.getOrderId(), order.getStatus()));

    }
}
