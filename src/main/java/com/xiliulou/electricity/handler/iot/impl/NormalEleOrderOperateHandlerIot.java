package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.LessTimeExchangeDTO;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.enums.OrderCheckEnum;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.utils.VersionUtil;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
    
    private static final String INIT_DEVICE_USING_MSG = "换电柜正在使用中操作取消";
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    
    @Autowired
    ElectricityCabinetOrderService electricityCabinetOrderService;
    
    public static final String ORDER_LESS_TIME_EXCHANGE_CABINET_VERSION="2.1.19";

    /**
     * 租电自主开仓操作记录中间版本；todo 柜机版本，用来兼容旧版本操作记录
     */
    public static final String RENT_RETURN_ORDER_SELF_OPEN_CABINET_VERSION = "2.4.0";


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        EleOrderOperateVO eleOrderOperateVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleOrderOperateVO.class);
        if (Objects.isNull(eleOrderOperateVO)) {
            log.error("ELE ERROR! eleOrderOperateVO is null,sessionId={}", receiverMessage.getSessionId());
            return;
        }
        
        if (receiverMessage.getType().equalsIgnoreCase(ElectricityIotConstant.API_ORDER_OPER_HISTORY)) {
        } else {
            Integer type = eleOrderOperateVO.getOrderType();
            Integer seq = eleOrderOperateVO.getSeq();
            if (Objects.equals(type, ElectricityCabinetOrderOperHistory.ORDER_TYPE_SELF_OPEN)) {
                ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderService.queryByOrderId(eleOrderOperateVO.getOrderId());
                if (Objects.nonNull(electricityCabinetOrder)) {
                    type = ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE;
                    if (VersionUtil.compareVersion(electricityCabinet.getVersion(), ORDER_LESS_TIME_EXCHANGE_CABINET_VERSION) >= 0) {
                        seq = eleOrderOperateVO.getSeq();
                    }else {
                        seq = ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ_SUCCESS;
                    }
                } else {
                    // todo 租退电兼容旧版本
                    type = ElectricityCabinetOrderOperHistory.ORDER_TYPE_RENT_BACK;
                    if (VersionUtil.compareVersion(electricityCabinet.getVersion(), RENT_RETURN_ORDER_SELF_OPEN_CABINET_VERSION) >= 0) {
                        seq = eleOrderOperateVO.getSeq();
                    } else {
                        seq = ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_BY_RETURN_BATTERY_COMPLETE;
                    }
                }
            }
            
            if (StringUtils.isNotBlank(eleOrderOperateVO.getOrderId()) && StringUtils.isNotBlank(eleOrderOperateVO.getMsg()) && eleOrderOperateVO.getMsg()
                    .contains(INIT_DEVICE_USING_MSG)) {
                return;
            }
            
            //加入操作记录表
            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder().createTime(eleOrderOperateVO.createTime)
                    .orderId(eleOrderOperateVO.getOrderId()).type(type).tenantId(electricityCabinet.getTenantId()).msg(eleOrderOperateVO.getMsg()).seq(seq)
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
        
        /**
         * 订单状态阶段
         */
        private String title;
        
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



