package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleWarnFactory;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @author: lxc
 * @Date: 2021/03/29 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_WARN_HANDLER)
@Slf4j
public class NormalWarnHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    EleWarnMsgService eleWarnMsgService;
    @Autowired
    EleWarnFactory eleWarnFactory;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        EleWarnVO eleWarnVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleWarnVO.class);
        if (Objects.isNull(eleWarnVO)) {
            log.error("ele warn error! no eleWarnVO,{}", receiverMessage.getOriginContent());
            return ;
        }

        //插入异常
        EleWarnMsg eleWarnMsg= EleWarnMsg.builder()
               .electricityCabinetId(electricityCabinet.getId())
                .electricityCabinetName(electricityCabinet.getName())
                .cellNo(eleWarnVO.getCellNo())
                .msg(eleWarnVO.getMsg())
                .type(eleWarnVO.getMsgType())
                .code(eleWarnVO.getCode())
                .status(EleWarnMsg.STATUS_UNREAD)
                .createTime(eleWarnVO.getCreateTime())
                .updateTime(eleWarnVO.getCreateTime())
                .tenantId(electricityCabinet.getTenantId())
                .build();
        eleWarnMsgService.insert(eleWarnMsg);
    }


    @Data
    class EleWarnVO {
        //仓门号
        private Integer cellNo;
        //报错信息
        private String msg;
        //错误类型
        private Integer msgType;

        private Long createTime;

        private Integer code;
    }
}



