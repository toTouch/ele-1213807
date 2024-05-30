package com.xiliulou.electricity.mq.producer;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xiliulou.electricity.dto.operate.OperateLogDTO;
import com.xiliulou.electricity.exception.UserOperateLogSendException;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.xiliulou.electricity.mq.constant.MqProducerConstant.USER_OPERATION_RECORD_LOG;

/**
 * <p>
 * Description: This class is OperationRecordProducer!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/3/25
 **/
@Slf4j
@Component
public class OperationRecordProducer {
    
    private final RocketMqService rocketMqService;
    
    public OperationRecordProducer(RocketMqService rocketMqService) {
        this.rocketMqService = rocketMqService;
    }
    
    public void sendMessage(OperateLogDTO operate) {
        log.info("operate:{}",operate);
        if (Objects.isNull(operate)) {
            return;
        }
        try {
            JSONObject obj = JSONUtil.parseObj(operate);
            Pair<Boolean, String> pair = rocketMqService.sendSyncMsg(USER_OPERATION_RECORD_LOG, obj.toString());
            if (!pair.getLeft()) {
                throw new UserOperateLogSendException();
            }
            log.debug("Successfully sent user operation records to the queue {}", obj);
        } catch (RuntimeException e) {
            log.error("Unable to send user operation records to the queue because: {}", e.getMessage());
        }
    }
}
