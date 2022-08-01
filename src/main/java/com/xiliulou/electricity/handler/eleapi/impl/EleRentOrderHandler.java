package com.xiliulou.electricity.handler.eleapi.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.EleApiConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.handler.eleapi.EleApiHandler;
import com.xiliulou.electricity.query.api.ApiRequestQuery;
import com.xiliulou.electricity.query.api.RentQuery;
import com.xiliulou.electricity.service.ApiRentOrderService;
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
 * @author: Miss.Li
 * @Date: 2021/11/5 14:07
 * @Description:
 */

@Service(value = EleApiConstant.RENT_ORDER)
@Slf4j
public class EleRentOrderHandler implements EleApiHandler {
    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    ApiRentOrderService apiRentOrderService;

    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;

    @Autowired
    RedisService redisService;


    @Override
    public Triple<Boolean, String, Object> handleCommand(ApiRequestQuery apiRequestQuery) {
        RentQuery rentQuery = JsonUtil.fromJson(apiRequestQuery.getData(), RentQuery.class);
        if (Objects.isNull(rentQuery) || StrUtil.isEmpty(rentQuery.getOrderId())) {
            log.error("ELE RENT ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "orderId不存在");
        }

        if (StrUtil.isNotEmpty(rentQuery.getType()) && BatteryConstant.existsBatteryType(rentQuery.getType())) {
            log.error("ELE RENT ORDER ERROR! no orderId! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1002", "type类型不合法");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByProductAndDeviceName(rentQuery.getProductKey(), rentQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet) || !TenantContextHolder.getTenantId().equals(electricityCabinet.getTenantId())) {
            log.error("ELE RENT ORDER ERROR! not found eleCabinet! requestId={},p={},d={} ct={},lt={}", apiRequestQuery.getRequestId(), rentQuery.getProductKey(), rentQuery.getDeviceName(),electricityCabinet.getTenantId(), TenantContextHolder.getTenantId());
            return Triple.of(false, "API.00002", "柜机不存在");
        }

        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("rentBattery  ERROR!  electricityCabinet is offline ！pd={}", electricityCabinet.getDeviceName());
            return Triple.of(false, "API.00002", "柜机不在线！");
        }

        ApiRentOrder apiRentOrder = apiRentOrderService.queryByOrderId(rentQuery.getOrderId(), TenantContextHolder.getTenantId());
        if (Objects.nonNull(apiRentOrder)) {
            log.error("ELE RENT ORDER ERROR! order_id is repeat! requestId={},orderId={}", apiRequestQuery.getRequestId(), rentQuery.getOrderId());
            return Triple.of(false, "API.10002", "订单号重复！");
        }

        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 3 * 60 * 1000L, false);
        if (!result) {
            log.error("ELE RENT ORDER ERROR! cabinet is busy! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "AUTH.1006", "柜机在使用中，请勿重复下单！");
        }

        Pair<Boolean, ElectricityCabinetBox> usableDistributeCell = electricityCabinetService.findUsableBatteryCellNo(electricityCabinet.getId(), rentQuery.getType(), electricityCabinet.getFullyCharged());
        if (!usableDistributeCell.getLeft()) {
            electricityCabinetService.unlockElectricityCabinet(electricityCabinet.getId());
            return Triple.of(false, "API.10001", "没有可以换电的电池");
        }

        ElectricityCabinetBox box = usableDistributeCell.getRight();

        ApiRentOrder order = ApiRentOrder.builder()
                .batterySn(box.getSn())
                .orderSeq(0.0)
                .batteryType(box.getBatteryType())
                .cellNo(Integer.parseInt(box.getCellNo()))
                .orderId(rentQuery.getOrderId())
                .eid(electricityCabinet.getId())
                .createTime(System.currentTimeMillis())
                .updateTime(System.currentTimeMillis())
                .tenantId(TenantContextHolder.getTenantId())
                .status(ApiRentOrder.STATUS_INIT)
                .build();
        apiRentOrderService.insert(order);


        HashMap<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("cellNo", order.getCellNo());
        dataMap.put("orderId", order.getOrderId());

        HardwareCommandQuery comm = HardwareCommandQuery.builder()
                .sessionId(apiRequestQuery.getRequestId())
                .data(dataMap)
                .productKey(electricityCabinet.getProductKey())
                .deviceName(electricityCabinet.getDeviceName())
                .command(ElectricityIotConstant.API_RENT_ORDER).build();
        Pair<Boolean, String> sendResult = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        if (!sendResult.getLeft()) {
            log.error("ELE RENT ORDER ERROR! send command error! requestId={}", apiRequestQuery.getRequestId());
            return Triple.of(false, "API.00003", sendResult.getRight());
        }

        return Triple.of(true, null, new ApiOrderVo(order.getOrderId(), order.getStatus()));

    }
}
