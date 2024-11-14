package com.xiliulou.electricity.mq.consumer;

import java.util.Objects;

import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.UserEleOnlineLog;
import com.xiliulou.electricity.mq.constant.MqConsumerConstant;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.UserEleOnlineLogService;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RocketMQMessageListener(topic = MqProducerConstant.USER_DEVICE_STATUS_TOPIC, consumerGroup = MqConsumerConstant.USER_DEVICE_STATUS_CONSUMER_GROUP, consumeThreadMax = 2)
public class UserOnlineLogConsumer implements RocketMQListener<String> {

    private static final long TWO_MINUTES_IN_MILLIS = 2 * 60 * 1000;

    @Autowired
    private UserEleOnlineLogService userEleOnlineLogService;

    @Autowired
    private MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    @Autowired
    private RedisService redisService;

    @Autowired
    private ElectricityCabinetService electricityCabinetService;

    @Override
    public void onMessage(String message) {
        try {
            TtlTraceIdSupport.set();
            UserEleOnlineLog eleOnlineLog = JsonUtil.fromJson(message, UserEleOnlineLog.class);
            if (Objects.isNull(eleOnlineLog)) {
                log.warn("Invalid message format: {}", message);
                return;
            }

            if (Objects.equals(eleOnlineLog.getStatus(), CommonConstant.STATUS_ONLINE)) {
                handleOnlineStatus(eleOnlineLog);
            } else {
                handleOfflineStatus(eleOnlineLog);
            }

        } catch (Exception e) {
            log.error("User online log consumer error! msg = {}", message, e);
        }finally {
            TtlTraceIdSupport.clear();
        }

    }

    private void handleOnlineStatus(UserEleOnlineLog eleOnlineLog) {
        userEleOnlineLogService.insert(eleOnlineLog);
        sendDeviceNotification(eleOnlineLog);
    }

    // Handle offline status
    private void handleOfflineStatus(UserEleOnlineLog eleOnlineLog)  {
        String userDeviceStatus = redisService.get(CacheConstant.CACHE_USER_DEVICE_STATUS + eleOnlineLog.getElectricityId());
        if (StrUtil.isEmpty(userDeviceStatus)) {
            return;
        }

        Pair<Integer, Long> userDeviceStatusValue = userEleOnlineLogService
                .parseUserDeviceStatusValue(userDeviceStatus);
        if (Objects.equals(userDeviceStatusValue.getLeft(), ElectricityCabinet.STATUS_ONLINE)) {
            return;
        }

        long currentTimestamp = System.currentTimeMillis();
        long lastOnlineTimestamp = userDeviceStatusValue.getRight();

        if (currentTimestamp - lastOnlineTimestamp > TWO_MINUTES_IN_MILLIS) {
            userEleOnlineLogService.insert(eleOnlineLog);
            sendDeviceNotification(eleOnlineLog);
        }
    }

    // Send device notification
    private void sendDeviceNotification(UserEleOnlineLog eleOnlineLog) {
        ElectricityCabinet cabinet = electricityCabinetService.queryByIdFromCache(eleOnlineLog.getElectricityId());
        if (Objects.isNull(cabinet)){
            log.info("cabinet not exit ! id={}",eleOnlineLog.getElectricityId());
            return;
        }
        maintenanceUserNotifyConfigService.sendDeviceNotify(cabinet, eleOnlineLog.getStatus(),
                eleOnlineLog.getAppearTime());
    }

}
