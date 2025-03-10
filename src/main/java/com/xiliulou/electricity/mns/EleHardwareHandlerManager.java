package com.xiliulou.electricity.mns;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.config.TenantConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.DeviceReportConstant;
import com.xiliulou.electricity.constant.EleCabinetConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleOnlineLog;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.UserEleOnlineLog;
import com.xiliulou.electricity.handler.iot.IElectricityHandler;
import com.xiliulou.electricity.mq.constant.MqProducerConstant;
import com.xiliulou.electricity.request.CabinetCommandRequest;
import com.xiliulou.electricity.service.EleOnlineLogService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.service.UserEleOnlineLogService;
import com.xiliulou.electricity.service.thirdParty.PushDataToThirdService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.Ipv4Util;
import com.xiliulou.feishu.config.FeishuConfig;
import com.xiliulou.feishu.service.FeishuMsgService;
import com.xiliulou.feishu.service.FeishuTokenService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.mns.HardwareHandlerManager;
import com.xiliulou.mq.service.RocketMqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author: lxc
 * @Date: 2020/12/28 13:26
 * @Description:
 */
@Service
@Slf4j
public class EleHardwareHandlerManager extends HardwareHandlerManager {
    
    /**
     * 命令映射处理的handler
     */
    @Autowired
    private Map<String, IElectricityHandler> electricityHandlerMap;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    TenantService tenantSerivce;
    
    @Autowired
    FeishuConfig feishuConfig;
    
    @Autowired
    FeishuMsgService feishuMsgService;
    
    @Autowired
    FeishuTokenService feishuTokenService;
    
    @Autowired
    TenantConfig tenantConfig;
    
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;
    
    @Autowired
    EleOnlineLogService eleOnlineLogService;
    
    @Autowired
    @Qualifier("deviceReportRestTemplate")
    RestTemplate restTemplate;
    
    @Autowired
    UserEleOnlineLogService userEleOnlineLogService;
    
    @Autowired
    private RocketMqService rocketMqService;
    
    @Resource
    private PushDataToThirdService pushDataToThirdService;
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("eleHardwareHandlerExecutor", 2, "ELE_HARDWARE_HANDLER_EXECUTOR");
    
    public Pair<Boolean, String> chooseCommandHandlerProcessSend(HardwareCommandQuery hardwareCommandQuery) {
        return this.chooseCommandHandlerProcessSend(hardwareCommandQuery, null);
    }
    
    public Pair<Boolean, String> chooseCommandHandlerProcessSend(HardwareCommandQuery hardwareCommandQuery, ElectricityCabinet electricityCabinet) {
        if (Objects.nonNull(electricityCabinet) && Objects.equals(electricityCabinet.getPattern(), EleCabinetConstant.TCP_PATTERN)) {
            return sendCommandToEleForTcp(hardwareCommandQuery);
        }
        
        IElectricityHandler electricityHandler = electricityHandlerMap.get(ElectricityIotConstant.acquireChargeHandlerName(hardwareCommandQuery.getCommand()));
        if (Objects.isNull(electricityHandler)) {
            log.error("ELE ERROR! command not support handle,command={}", hardwareCommandQuery.getCommand());
            return Pair.of(false, "发送失败，命令不存在！");
        }
        
        return electricityHandler.handleSendHardwareCommand(hardwareCommandQuery);
    }
    
    @Override
    public boolean chooseCommandHandlerProcessReceiveMessage(ReceiverMessage receiverMessage) {
        // 更新柜机状态
        updateElectricityCabinetStatus(receiverMessage);
        
        IElectricityHandler electricityHandler = electricityHandlerMap.get(ElectricityIotConstant.acquireChargeHandlerName(receiverMessage.getType()));
        if (Objects.isNull(electricityHandler)) {
            if (!ElectricityIotConstant.isLegalCommand(receiverMessage.getType())) {
                log.warn("ELE WARNNING!command not support handle,command:{}", receiverMessage.getType());
            }
            return false;
        }
        
        return electricityHandler.receiveMessageProcess(receiverMessage);
    }
    
    private void updateElectricityCabinetStatus(ReceiverMessage receiverMessage) {
        if (StringUtils.isNotBlank(receiverMessage.getType())) {
            return;
        }
        
        // 电柜在线状态
        executorService.execute(() -> {
            if (StringUtils.isBlank(receiverMessage.getStatus())) {
                return;
            }
            
            ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(receiverMessage.getProductKey(),
                    receiverMessage.getDeviceName());
            if (Objects.isNull(electricityCabinet)) {
                log.warn("ELE WARN! no product and device ,p={},d={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
                return;
            }
            
            // 在线状态修改
            ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
            newElectricityCabinet.setProductKey(electricityCabinet.getProductKey());
            newElectricityCabinet.setDeviceName(electricityCabinet.getDeviceName());
            newElectricityCabinet.setId(electricityCabinet.getId());
            newElectricityCabinet.setOnlineStatus(CommonConstant.STATUS_ONLINE.equals(receiverMessage.getStatus()) ? 0 : 1);
            newElectricityCabinet.setUpdateTime(DateUtils.parseMillsDateStrToTimestampV2(receiverMessage.getTime()));
            // 柜机模式修改
            if (CommonConstant.STATUS_ONLINE.equals(receiverMessage.getStatus())) {
                newElectricityCabinet.setPattern(EleCabinetConstant.ALI_IOT_PATTERN);
            }
            
            if (electricityCabinet.getUpdateTime() <= newElectricityCabinet.getUpdateTime()) {
                electricityCabinetService.update(newElectricityCabinet);
            }
            
            addOnlineLogAndHandleUserOnlineLogMessage(receiverMessage, newElectricityCabinet, electricityCabinet.getTenantId());
        });
    }
    
    private void addOnlineLogAndHandleUserOnlineLogMessage(ReceiverMessage receiverMessage, ElectricityCabinet electricityCabinet, Integer tenantId) {
        EleOnlineLog eleOnlineLog = new EleOnlineLog();
        eleOnlineLog.setElectricityId(electricityCabinet.getId());
        eleOnlineLog.setClientIp(receiverMessage.getClientIp());
        eleOnlineLog.setStatus(receiverMessage.getStatus());
        eleOnlineLog.setAppearTime(receiverMessage.getTime());
        eleOnlineLog.setCreateTime(System.currentTimeMillis());
        eleOnlineLog.setMsg(receiverMessage.getStatus());
        eleOnlineLogService.insert(eleOnlineLog);
        
        redisService.set(CacheConstant.CACHE_USER_DEVICE_STATUS + electricityCabinet.getId(),
                userEleOnlineLogService.generateUserDeviceStatusValue(electricityCabinet.getOnlineStatus(), electricityCabinet.getUpdateTime()), 48L, TimeUnit.HOURS);
        
        // 这里需要设置ID为空，方便consumner插入正确数据
        eleOnlineLog.setId(null);
        UserEleOnlineLog lastUserEleOnlineLog = userEleOnlineLogService.queryLastLog(electricityCabinet.getId());
        int delayType = 1; // 默认延迟1秒
        boolean shouldSendMessage = false;
        
        if (Objects.isNull(lastUserEleOnlineLog)) {
            // 如果没有记录，代表表里是第一条信息，则直接发送
            shouldSendMessage = true;
        } else {
            boolean isCurrentlyOnline = Objects.equals(electricityCabinet.getOnlineStatus(), ElectricityCabinet.STATUS_ONLINE);
            boolean wasOffline = Objects.equals(lastUserEleOnlineLog.getStatus(), CommonConstant.STATUS_OFFLINE);
            
            if (isCurrentlyOnline && wasOffline) {
                // 如果上一次是离线，并且这一次是上线，则发送MQ
                shouldSendMessage = true;
            } else if (!isCurrentlyOnline) {
                // 如果这一次是离线状态，无论上一次是什么状态，都发送MQ，并延迟2分钟
                shouldSendMessage = true;
                delayType = 6; // 离线延迟2分钟
            }
        }
        
        if (shouldSendMessage) {
            rocketMqService.sendAsyncMsg(MqProducerConstant.USER_DEVICE_STATUS_TOPIC, JsonUtil.toJson(eleOnlineLog), null, null, delayType);
            
            // 给第三方推送柜机上下线状态
            pushDataToThirdService.asyncPushCabinetStatus(receiverMessage.getSessionId(), tenantId, electricityCabinet.getId().longValue(), delayType);
        }
        
        // 发送MQ通知
        // maintenanceUserNotifyConfigService.sendDeviceNotifyMq(electricityCabinet,
        // receiverMessage.getStatus(),
        // receiverMessage.getTime());·
    }
    
    public Pair<Boolean, String> sendCommandToEleForTcp(HardwareCommandQuery query) {
        String serverIp = electricityCabinetService.acquireDeviceBindServerIp(query.getProductKey(), query.getDeviceName());
        if (org.apache.commons.lang3.StringUtils.isBlank(serverIp) || Objects.equals(Ipv4Util.ipv4ToLong(serverIp, 0), 0)) {
            log.warn("ELE WARN! device's server ip not found! txnNo={},p={},d={},ip={}", query.getSessionId(), query.getProductKey(), query.getDeviceName(), serverIp);
            return Pair.of(false, "设备IP不存在");
        }
        
        Map params = null;
        if (Objects.nonNull(query.getData()) && query.getData() instanceof Map) {
            params = (Map) query.getData();
        }
        
        CabinetCommandRequest request = new CabinetCommandRequest();
        request.setProductKey(query.getProductKey());
        request.setDeviceName(query.getDeviceName());
        request.setSessionId(query.getSessionId());
        request.setType(query.getCommand());
        request.setContent(params);
        
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.APPLICATION_JSON;
        headers.setContentType(type);
        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtil.toJson(request), headers);
        ResponseEntity<R> rResponseEntity = null;
        try {
            rResponseEntity = restTemplate.postForEntity("http://" + serverIp + ":" + DeviceReportConstant.REPORT_SERVER_PORT + DeviceReportConstant.REPORT_SERVER_CONTEXT_PATH
                    + DeviceReportConstant.REPORT_SERVER_SEND_COMMAND_URL, httpEntity, R.class);
            if (!rResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                log.warn("ELE SEND TCP COMMAND WARN! call device server error! p={},d={},ip={} msg={}", query.getProductKey(), query.getDeviceName(), serverIp,
                        rResponseEntity.getBody());
                return Pair.of(false, "设备消息发送失败");
            }
            
            R result = rResponseEntity.getBody();
            if (Objects.isNull(result) || !result.isSuccess()) {
                log.warn("ELE SEND TCP COMMAND WARN! call device rsp fail! p={},d={},ip={} msg={}", query.getProductKey(), query.getDeviceName(), serverIp,
                        rResponseEntity.getBody());
                return Pair.of(false, "设备消息发送失败");
            }
            
            log.info("ELE SEND TCP COMMAND INFO!send command success!p={},d={},ip={} msg={}", query.getProductKey(), query.getDeviceName(), serverIp, rResponseEntity.getBody());
        } catch (RestClientException e) {
            log.error("ELE SEND TCP COMMAND ERROR! send command fail! p={},d={},ip={}", query.getProductKey(), query.getDeviceName(), serverIp, e);
            return Pair.of(false, "设备消息发送失败");
        }
        
        return Pair.of(true, null);
    }
}
