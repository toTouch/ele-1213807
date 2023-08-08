package com.xiliulou.electricity.mq.consumer;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.dto.UserCouponDTO;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.UserCouponService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 用户优惠券的 Consumer
 * @author xiaohui.song
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.USER_COUPON_COMMON_TOPIC, consumerGroup = MqConsumerConstant.USER_COUPON_COMMON_CONSUMER_GROUP)
public class UserCouponConsumer implements RocketMQListener<String> {

    XllThreadPoolExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("SEND_COUPON_CONSUMER_POOL", 5, "send_coupon_consumer_thread");

    @Autowired
    private UserCouponService userCouponService;

    @Override
    public void onMessage(String message) {
        try {
            UserCouponDTO userCouponDTO = JsonUtil.fromJson(message, UserCouponDTO.class);
            if (Objects.isNull(userCouponDTO)) {
                return;
            }

            executorService.execute(() -> {
                userCouponService.sendCouponToUser(userCouponDTO.getUid(), userCouponDTO.getCouponId());
            });

        } catch (Exception e){
            log.error("Send coupon consumer error! msg = {}", message, e);
        }

    }
}
