package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.EleOpenDTO;
import com.xiliulou.electricity.dto.EleOpenDTO.EleOpenDTOBuilder;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.queue.EleOperateQueueHandler;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import com.xiliulou.electricity.vo.WarnMsgVo;
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
        Boolean result = redisService.setNx(ElectricityCabinetConstant.SELF_OPEN_CALL_CACHE_KEY + eleSelfOPenCellOrderVo.getOrderId() + receiverMessage.getType(), "true", 10 * 1000L, true);
        if (!result) {
            log.error("OFFLINE EXCHANGE orderId is lock,{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }

        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(eleSelfOPenCellOrderVo.getOrderId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("SELF OPEN CELL ERROR! not found order! orderId:{}", eleSelfOPenCellOrderVo.getOrderId());
            return;
        }

        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecordUpdate = new ElectricityExceptionOrderStatusRecord();
        electricityExceptionOrderStatusRecordUpdate.setId(electricityExceptionOrderStatusRecord.getId());
        electricityExceptionOrderStatusRecordUpdate.setUpdateTime(System.currentTimeMillis());
        if (Objects.equals(eleSelfOPenCellOrderVo.getStatus(), ElectricityExceptionOrderStatusRecord.STATUS_OPEN_FAIL) || Objects.equals(eleSelfOPenCellOrderVo.getStatus(), ElectricityExceptionOrderStatusRecord.BATTERY_NOT_MATCH)) {
            electricityExceptionOrderStatusRecordUpdate.setOpenCellStatus(ElectricityExceptionOrderStatusRecord.OPEN_CELL_FAIL);
        }
        electricityExceptionOrderStatusRecordUpdate.setIsSelfOpenCell(ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL);
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


