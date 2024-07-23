package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author: renhang
 * @Date: 2024/07/23
 * @Description: 打开满电仓
 */
@Service(value = ElectricityIotConstant.NORMAL_OPEN_FULL_CELL_HANDLER)
@Slf4j
public class NormalOpenFullyCellHandlerIot extends AbstractElectricityIotHandler {
    
    @Autowired
    RedisService redisService;
    
    @Resource
    private ElectricityCabinetOrderService cabinetOrderService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("normalOpenFullyCellHandlerIot error! sessionId is null");
            return;
        }
        
        EleOpenFullCellRsp openFullCellRsp = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOpenFullCellRsp.class);
        if (Objects.isNull(openFullCellRsp)) {
            log.error("normalOpenFullyCellHandlerIot error! openFullCellRsp is null");
            return;
        }
        
        if (!redisService.setNx(CacheConstant.OPEN_FULL_CELL_LIMIT + openFullCellRsp.getOrderId(), "1", 200L, false)) {
            log.info("normalOpenFullyCellHandlerIot.order is being processed,requestId={},orderId={}", receiverMessage.getSessionId(), openFullCellRsp.getOrderId());
            return;
        }
        
        ElectricityCabinetOrder cabinetOrder = cabinetOrderService.queryByOrderId(openFullCellRsp.getOrderId());
        if (Objects.isNull(cabinetOrder)) {
            log.error("normalOpenFullyCellHandlerIot error! order is null,sessionId is{}, orderId is {}", sessionId, openFullCellRsp.getOrderId());
        }
        
        // 修改订单最终状态为成功
        if (Objects.nonNull(openFullCellRsp.getIsOpenSuccess()) && openFullCellRsp.getIsOpenSuccess()) {
            ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
            newElectricityCabinetOrder.setId(cabinetOrder.getId());
            newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
            newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS);
            newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_COMPLETE_OPEN_SUCCESS);
            cabinetOrderService.update(newElectricityCabinetOrder);
        }
        
    }
    
    
    @Data
    class EleOpenFullCellRsp {
        
        private String sessionId;
        
        //订单Id
        private String orderId;
        
        // 是否开门成功
        private Boolean isOpenSuccess;
        
        private String batterySn;
    }
    
}




