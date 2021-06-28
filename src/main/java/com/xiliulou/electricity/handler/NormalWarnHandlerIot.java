package com.xiliulou.electricity.handler;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.service.EleWarnFactory;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.entity.SendHardwareMessage;
import com.xiliulou.iot.service.AbstractIotMessageHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Objects;


/**
 * @author: lxc
 * @Date: 2021/03/29 17:02
 * @Description:
 */
@Service
@Slf4j
public class NormalWarnHandlerIot extends AbstractIotMessageHandler {
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    EleWarnMsgService eleWarnMsgService;
    @Autowired
    EleWarnFactory eleWarnFactory;



    @Override
    protected Pair<SendHardwareMessage, String> generateMsg(HardwareCommandQuery hardwareCommandQuery) {
        String sessionId = generateSessionId(hardwareCommandQuery);
        SendHardwareMessage message = SendHardwareMessage.builder()
                .sessionId(sessionId)
                .type(hardwareCommandQuery.getCommand())
                .data(hardwareCommandQuery.getData()).build();
        return Pair.of(message, sessionId);
    }

    @Override
    protected boolean receiveMessageProcess(ReceiverMessage receiverMessage) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(), receiverMessage.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return false;
        }
        EleWarnVO eleWarnVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleWarnVO.class);
        if (Objects.isNull(eleWarnVO)) {
            log.error("ele warn error! no eleWarnVO,{}", receiverMessage.getOriginContent());
            return false;
        }

        //插入异常
        EleWarnMsg eleWarnMsg= EleWarnMsg.builder()
               .electricityCabinetId(electricityCabinet.getId())
                .electricityCabinetName(electricityCabinet.getName())
                .cellNo(eleWarnVO.getCellNo())
                .msg(eleWarnVO.getMsg())
                .type(eleWarnVO.getMsgType())
                .status(EleWarnMsg.STATUS_UNREAD)
                .createTime(eleWarnVO.getCreateTime())
                .updateTime(eleWarnVO.getCreateTime())
                .tenantId(electricityCabinet.getTenantId())
                .build();
        eleWarnMsgService.insert(eleWarnMsg);
        return true;
    }

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

}

