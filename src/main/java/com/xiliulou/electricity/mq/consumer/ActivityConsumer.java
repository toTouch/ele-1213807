package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.dto.ActivityProcessDTO;
import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 活动的 Consumer
 *
 * @author xiaohui.song
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.ACTIVITY_COMMON_TOPIC, consumerGroup = MqConsumerConstant.ACTIVITY_COMMON_CONSUMER_GROUP)
public class ActivityConsumer implements RocketMQListener<String> {
    //XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("ACTIVITY_HANDLE_CONSUMER_POOL", 5, "activity_handle_thread");
    @Autowired
    private ActivityService activityService;

    @Override
    public void onMessage(String message) {
        try {

            ActivityProcessDTO activityProcessDTO = JsonUtil.fromJson(message, ActivityProcessDTO.class);
            if (Objects.isNull(activityProcessDTO)) {
                return;
            }

            if(ActivityEnum.INVITATION_CRITERIA_BUY_PACKAGE.getCode().equals(activityProcessDTO.getActivityType())){

                activityService.handleActivityByPackage(activityProcessDTO.getOrderNo(), activityProcessDTO.getType());
            }else if(ActivityEnum.INVITATION_CRITERIA_REAL_NAME.getCode().equals(activityProcessDTO.getActivityType())){
                activityService.handleActivityByRealName(activityProcessDTO.getUid());
            }

        } catch (Exception e){
            log.error("Activity handle consumer error! msg = {}", message, e);
        }
    }
}
