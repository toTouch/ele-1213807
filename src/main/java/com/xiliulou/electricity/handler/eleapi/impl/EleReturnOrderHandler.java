package com.xiliulou.electricity.handler.eleapi.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.EleApiConstant;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ApiReturnOrder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.HardwareCommand;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.query.api.ReturnQuery;
import com.xiliulou.electricity.service.ApiReturnOrderService;
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
@Service(value = EleApiConstant.RETURN_ORDER)
@Slf4j
public class EleReturnOrderHandler implements EleApiHandler {
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ApiReturnOrderService apiReturnOrderService;
    @Autowired
    RedisService redisService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;


    @Override
    public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {
        ReturnQuery rentQuery = JsonUtil.fromJson(apiRequestQuery.getData(), ReturnQuery.class);
        if (StrUtil.isEmpty(rentQuery.getOrderId())) {
            log.error("ELE RETURN ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "orderId不存在");
        }

        if (StrUtil.isNotEmpty(rentQuery.getType()) && BatteryConstant.existsBatteryType(rentQuery.getType())) {
            log.error("ELE RETURN ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "type类型不合法");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(rentQuery.getProductKey(), rentQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet) || !TenantContextHolder.getTenantId().equals(electricityCabinet.getTenantId())) {
            log.error("ELE RETURN ORDER ERROR! not found eleCabinet! requestId={},p={},d={}", apiRequestQuery.getRequestId(), rentQuery.getProductKey(), rentQuery.getDeviceName());
            return Triple.of(false, "API.00002", "柜机不存在");
        }

        if (StrUtil.isNotEmpty(rentQuery.getReturnBatteryName()) && Objects.isNull(electricityBatteryService.queryBySn(rentQuery.getReturnBatteryName()))) {
            log.error("ELE RETURN ORDER ERROR! returnBattery's sn not found! requestId={},batteryName={}", apiRequestQuery.getRequestId(), rentQuery.getReturnBatteryName());
            return Triple.of(false, "API.10003", "归还的电池不存在系统");
        }

        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELE RETURN ORDER ERROR!  electricityCabinet is offline ！pd={}", electricityCabinet.getDeviceName());
            return Triple.of(false, "API.00002", "柜机不在线！");
        }

        ApiReturnOrder apiReturnOrder = apiReturnOrderService.queryByOrderId(rentQuery.getOrderId(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(apiReturnOrder)) {
            log.error("ELE RETURN ORDER ERROR! order_id is repeat! requestId={},orderId={}", apiRequestQuery.getRequestId(), rentQuery.getOrderId());
            return Triple.of(false, "API.10002", "订单号重复！");
        }

        boolean result = redisService.setNx(ElectricityCabinetConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 3 * 60 * 1000L, false);
        if (!result) {
            log.error("ELE RENT ORDER ERROR! cabinet is busy! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1006", "柜机在使用中，请勿重复下单！");
        }

        Pair<Boolean, Integer> usableEmptyCellResult = electricityCabinetService.findUsableEmptyCellNo(electricityCabinet.getId());
        if (!usableEmptyCellResult.getLeft()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            return Triple.of(false, "Api10001", "没有空的仓门");
        }

        ApiReturnOrder order = ApiReturnOrder.builder()
                .orderSeq(0.0)
                .batterySn(rentQuery.getReturnBatteryName())
                .batteryType(rentQuery.getType())
                .orderId(rentQuery.getOrderId())
                .cellNo(usableEmptyCellResult.getRight())
                .createTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .updateTime(System.currentTimeMillis())
                .eid(electricityCabinet.getId())
                .status(ApiReturnOrder.STATUS_INIT)
                .build();
        apiReturnOrderService.insert(order);

        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cellNo", order.getCellNo());
        dataMap.put("orderId", order.getOrderId());
        dataMap.put("isModelType", rentQuery.getType() != null);
        dataMap.put("multiBatteryModelName", rentQuery.getType());
        dataMap.put("returnBatteryName",rentQuery.getReturnBatteryName());


        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(apiRequestQuery.getRequestId())
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(HardwareCommand.API_RETURN_ORDER).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("ELE RETURN ORDER ERROR! send command error! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "API.00003", sendResult.getRight());
        }

        return Triple.of(true, null, new ApiOrderVo(order.getOrderId(), order.getStatus()));
    }
}

