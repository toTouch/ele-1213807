package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: hrp
 * @Date: 2022/07/22 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_ORDER_SELF_OPEN_CELL_HANDLER)
@Slf4j
public class NormalEleSelfOpenCellHandlerIot extends AbstractElectricityIotHandler {


    @Autowired
    RedisService redisService;
    @Autowired
    EleOperateQueueHandler eleOperateQueueHandler;

    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;

    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;

    @Autowired
    ElectricityBatteryService electricityBatteryService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("SELF OPEN CELL EXCHANGE NO sessionId,{}", receiverMessage.getOriginContent());
            return;
        }

        EleSelfOPenCellOrderVo eleSelfOPenCellOrderVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleSelfOPenCellOrderVo.class);

        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityExceptionOrderStatusRecord)) {
            log.error("SELF OPEN CELL ERROR! not found user! userId:{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }

        //幂等加锁
        Boolean result = redisService.setNx(CacheConstant.SELF_OPEN_CALL_CACHE_KEY + eleSelfOPenCellOrderVo.getOrderId() + receiverMessage.getType(), "true", 10 * 1000L, true);
        if (!result) {
            log.error("OFFLINE EXCHANGE orderId is lock,{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }

        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("SELF OPEN CELL ERROR! not found order! orderId:{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }

        //操作回调的放在redis中,记录开门结果
        if (Objects.nonNull(eleSelfOPenCellOrderVo.getResult()) && eleSelfOPenCellOrderVo.getResult()) {
            redisService.set(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId, "true", 30L, TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId, "false", 30L, TimeUnit.SECONDS);
        }

        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecordUpdate = new ElectricityExceptionOrderStatusRecord();
        electricityExceptionOrderStatusRecordUpdate.setId(electricityExceptionOrderStatusRecord.getId());
        electricityExceptionOrderStatusRecordUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.equals(eleSelfOPenCellOrderVo.getStatus(), ElectricityExceptionOrderStatusRecord.STATUS_OPEN_FAIL) || Objects.equals(eleSelfOPenCellOrderVo.getStatus(), ElectricityExceptionOrderStatusRecord.BATTERY_NOT_MATCH)) {
            electricityExceptionOrderStatusRecordUpdate.setOpenCellStatus(ElectricityExceptionOrderStatusRecord.OPEN_CELL_FAIL);
        }
        electricityExceptionOrderStatusRecordService.update(electricityExceptionOrderStatusRecordUpdate);
    }

    @Data
    class EleSelfOPenCellOrderVo {

        //订单Id
        private String orderId;
        //orderStatus
        private String status;
        //msg
        private String msg;

        private Integer cellNo;

        private String batteryName;

        private Boolean result;
    }
}


