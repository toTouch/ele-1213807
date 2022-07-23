package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ApiOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_ORDER_OPERATE_HANDLER)
@Slf4j
public class NormalEleOrderOperateHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    @Autowired
    ApiOrderOperHistoryService apiOrderOperHistoryService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        EleOrderOperateVO eleOrderOperateVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOrderOperateVO.class);
        if (Objects.isNull(eleOrderOperateVO)) {
            log.error("ELE ERROR! eleOrderOperateVO is null,sessionId={}", receiverMessage.getSessionId());
            return;
        }

        if (receiverMessage.getType().equalsIgnoreCase(ElectricityIotConstant.API_ORDER_OPER_HISTORY)) {
            ApiOrderOperHistory history = ApiOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(eleOrderOperateVO.getOrderId())
                    .type(eleOrderOperateVO.getOrderType())
                    .tenantId(electricityCabinet.getTenantId())
                    .msg(eleOrderOperateVO.getMsg())
                    .build();
            apiOrderOperHistoryService.insert(history);
        } else {

            Integer type = eleOrderOperateVO.getOrderType();
            Integer seq = eleOrderOperateVO.getSeq();
            if (Objects.equals(type, ElectricityCabinetOrderOperHistory.ORDER_TYPE_SELF_OPEN)) {
                type = ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE;
                seq = ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ_COMPLETE;
            }


            //加入操作记录表
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(eleOrderOperateVO.getOrderId())
                    .type(type)
                    .tenantId(electricityCabinet.getTenantId())
                    .msg(eleOrderOperateVO.getMsg())
                    .seq(seq)
                    .result(eleOrderOperateVO.getResult()).build();
            electricityCabinetOrderOperHistoryService.insert(history);
        }
    }


    @Data
    class EleOrderOperateVO {
        //订单Id
        private String orderId;
        //
        private Long createTime;
        //
        private Integer orderType;
        //msg
        private String msg;
        /**
         * 操作步骤序号
         */
        private Integer seq;
        /**
         * 操作结果 0：成功，1：失败
         */
        private Integer result;
    }
}



