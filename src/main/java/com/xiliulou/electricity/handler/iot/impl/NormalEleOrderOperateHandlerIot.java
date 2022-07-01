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

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_ELE_ORDER_OPERATE_HANDLER)
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
            //加入操作记录表
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(eleOrderOperateVO.getOrderId())
                    .type(eleOrderOperateVO.getOrderType())
                    .tenantId(electricityCabinet.getTenantId())
                    .msg(eleOrderOperateVO.getMsg())
                    .build();
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
    }
}



