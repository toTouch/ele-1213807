package com.xiliulou.electricity.mns;

import com.google.common.collect.Lists;
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
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.handler.iot.IElectricityHandler;
import com.xiliulou.electricity.request.CabinetCommandRequest;
import com.xiliulou.electricity.service.EleOnlineLogService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.MaintenanceUserNotifyConfigService;
import com.xiliulou.electricity.service.TenantService;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.DeviceTextUtil;
import com.xiliulou.electricity.utils.Ipv4Util;
import com.xiliulou.feishu.config.FeishuConfig;
import com.xiliulou.feishu.entity.query.FeishuBotSendMsgQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostSubQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostTextQuery;
import com.xiliulou.feishu.entity.query.msg.FeishuMsgPostTypeQuery;
import com.xiliulou.feishu.entity.rsp.FeishuTokenRsp;
import com.xiliulou.feishu.exception.FeishuException;
import com.xiliulou.feishu.service.FeishuMsgService;
import com.xiliulou.feishu.service.FeishuTokenService;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.iot.mns.HardwareHandlerManager;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    
    ExecutorService executorService = XllThreadPoolExecutors.newFixedThreadPool("eleHardwareHandlerExecutor", 2, "ELE_HARDWARE_HANDLER_EXECUTOR");
    
    public Pair<Boolean, String> chooseCommandHandlerProcessSend(HardwareCommandQuery hardwareCommandQuery) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(hardwareCommandQuery.getProductKey(),
                hardwareCommandQuery.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELE ERROR! not found electricityCabinet,p={},d={}", hardwareCommandQuery.getProductKey(), hardwareCommandQuery.getDeviceName());
            return Pair.of(false, "未找到换电柜！");
        }
        
        if (Objects.equals(electricityCabinet.getPattern(), EleCabinetConstant.TCP_PATTERN)) {
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
        //更新柜机状态
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
        
        //电柜在线状态
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
            
            if (redisService.hasKey(CacheConstant.CACHE_OFFLINE_KEY + electricityCabinet.getId())) {
                log.warn("ELE WARN! device is repeat report status! p={},d={},status={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName(),
                        receiverMessage.getStatus());
                return;
            }
            
            //在线状态修改
            ElectricityCabinet newElectricityCabinet = new ElectricityCabinet();
            newElectricityCabinet.setProductKey(electricityCabinet.getProductKey());
            newElectricityCabinet.setDeviceName(electricityCabinet.getDeviceName());
            newElectricityCabinet.setId(electricityCabinet.getId());
            newElectricityCabinet.setOnlineStatus(CommonConstant.STATUS_ONLINE.equals(receiverMessage.getStatus()) ? 0 : 1);
            newElectricityCabinet.setUpdateTime(DateUtils.parseMillsDateStrToTimestampV2(receiverMessage.getTime()));
            
            EleOnlineLog eleOnlineLog = new EleOnlineLog();
            eleOnlineLog.setElectricityId(electricityCabinet.getId());
            eleOnlineLog.setClientIp(receiverMessage.getClientIp());
            eleOnlineLog.setStatus(receiverMessage.getStatus());
            eleOnlineLog.setAppearTime(receiverMessage.getTime());
            eleOnlineLog.setCreateTime(System.currentTimeMillis());
            eleOnlineLog.setMsg(receiverMessage.getStatus());
            eleOnlineLogService.insert(eleOnlineLog);
            
            //柜机模式修改
            if (CommonConstant.STATUS_ONLINE.equals(receiverMessage.getStatus())) {
                newElectricityCabinet.setPattern(EleCabinetConstant.IOT_PATTERN);
                //从TCP列表中移除
                redisService.deleteInList(CacheConstant.CACHE_TCP_CABINET_LIST, 0, String.class,
                        DeviceTextUtil.assembleSn(receiverMessage.getProductKey(), receiverMessage.getDeviceName()));
            }
            
            if (electricityCabinet.getUpdateTime() <= newElectricityCabinet.getUpdateTime()) {
                electricityCabinetService.update(newElectricityCabinet);
                redisService.set(CacheConstant.CACHE_OFFLINE_KEY + electricityCabinet.getId(), "1", 30L, TimeUnit.SECONDS);
            }
            
            //            feishuSendMsg(electricityCabinet, receiverMessage.getStatus(), receiverMessage.getTime());
            
            //TODO 发送MQ通知
            maintenanceUserNotifyConfigService.sendDeviceNotifyMq(electricityCabinet, receiverMessage.getStatus(), receiverMessage.getTime());
        });
    }
    
    private void feishuSendMsg(ElectricityCabinet electricityCabinet, String onlineStatus, String time) {
        //租户不发上下线通知
        List<Integer> tenantIdList = tenantConfig.getDisableRobotMessageForTenantId();
        if (Objects.nonNull(tenantIdList) && tenantIdList.contains(electricityCabinet.getTenantId())) {
            return;
        }
        
        Tenant tenantEntity = tenantSerivce.queryByIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(tenantEntity)) {
            log.error("FEI SHU ERROR! tenant is empty error! cid={},tid={}", electricityCabinet.getId(), electricityCabinet.getTenantId());
            return;
        }
        
        String token = null;
        try {
            token = this.acquireAccessToken();
        } catch (FeishuException e) {
            log.error("FEI SHU ERROR! FAILED TO GET TOKEN", e);
            return;
        }
        
        FeishuMsgPostTextQuery query0 = new FeishuMsgPostTextQuery();
        query0.setText("产品系列：换电柜");
        
        FeishuMsgPostTextQuery query1 = new FeishuMsgPostTextQuery();
        query1.setText("柜机名称：" + electricityCabinet.getName());
        
        FeishuMsgPostTextQuery query2 = new FeishuMsgPostTextQuery();
        query2.setText("租户名称：" + tenantEntity.getName());
        
        FeishuMsgPostTextQuery query3 = new FeishuMsgPostTextQuery();
        query3.setText("当前状态：" + getOnlineStatus(onlineStatus));
        
        FeishuMsgPostTextQuery query4 = new FeishuMsgPostTextQuery();
        query4.setText(getOnlineStatus(onlineStatus) + "时间：" + time);
        
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine0 = Lists.newArrayList(query0);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine1 = Lists.newArrayList(query1);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine2 = Lists.newArrayList(query2);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine3 = Lists.newArrayList(query3);
        List<FeishuMsgPostTypeQuery> feishuMsgPostTypeLine4 = Lists.newArrayList(query4);
        
        FeishuMsgPostSubQuery feishuMsgPostSubQuery = new FeishuMsgPostSubQuery();
        feishuMsgPostSubQuery.setTitle("设备上下线通知");
        feishuMsgPostSubQuery.setContent(Arrays.asList(feishuMsgPostTypeLine0, feishuMsgPostTypeLine1, feishuMsgPostTypeLine2, feishuMsgPostTypeLine3, feishuMsgPostTypeLine4));
        
        FeishuMsgPostQuery feishuMsgPostQuery = new FeishuMsgPostQuery();
        feishuMsgPostQuery.setZhCn(feishuMsgPostSubQuery);
        
        FeishuBotSendMsgQuery botSendMsgQuery = new FeishuBotSendMsgQuery();
        botSendMsgQuery.setReceiveIdType(FeishuBotSendMsgQuery.TYPE_CHAT_ID);
        botSendMsgQuery.setMsgType(FeishuBotSendMsgQuery.MSG_POST);
        botSendMsgQuery.setContent(JsonUtil.toJson(feishuMsgPostQuery));
        
        List<String> receiveIds = feishuConfig.getReceiveIds();
        if (!CollectionUtils.isEmpty(receiveIds)) {
            for (String receiveId : receiveIds) {
                botSendMsgQuery.setReceiveId(receiveId);
                try {
                    feishuMsgService.sendBotMsg(botSendMsgQuery, token);
                } catch (FeishuException e) {
                    log.error("FEI SHU ERROR! FEI SHU SEND BOT MSG ERROR!", e);
                }
            }
        }
        
        return;
    }
    
    private String acquireAccessToken() throws FeishuException {
        String token = redisService.get(CacheConstant.CACHE_FEISHU_ACCESS_TOKEN);
        
        if (StringUtils.isBlank(token)) {
            FeishuTokenRsp feishuTokenRsp = feishuTokenService.acquireAccessToken();
            token = feishuTokenRsp.getTenantAccessToken();
            redisService.set(CacheConstant.CACHE_FEISHU_ACCESS_TOKEN, token, 1800L, TimeUnit.SECONDS);
        }
        
        return token;
    }
    
    private String getOnlineStatus(String status) {
        String str = "";
        switch (status) {
            case CommonConstant.STATUS_ONLINE:
                str = "上线";
                break;
            case CommonConstant.STATUS_OFFLINE:
                str = "下线";
                break;
        }
        return str;
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
            params.put("type", query.getCommand());
            params.put("sessionId", query.getSessionId());
        }
        
        CabinetCommandRequest request = new CabinetCommandRequest();
        request.setProductKey(query.getProductKey());
        request.setDeviceName(query.getDeviceName());
        request.setSessionId(query.getSessionId());
        request.setContent(params);
        
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.APPLICATION_JSON;
        headers.setContentType(type);
        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtil.toJson(request), headers);
        ResponseEntity<R> rResponseEntity = restTemplate.postForEntity(
                "http://" + serverIp + ":" + DeviceReportConstant.REPORT_SERVER_PORT + DeviceReportConstant.REPORT_SERVER_CONTEXT_PATH
                        + DeviceReportConstant.REPORT_SERVER_SEND_COMMAND_URL, httpEntity, R.class);
        if (!rResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
            log.warn("ELE WARN! call device server error! p={},d={},ip={} msg={}", query.getProductKey(), query.getDeviceName(), serverIp, rResponseEntity.getBody());
            return Pair.of(false, "设备消息发送失败");
        }
        
        R result = rResponseEntity.getBody();
        if (Objects.isNull(result) || !result.isSuccess()) {
            log.warn("ELE SEND COMMAND WARN! call device rsp fail! p={},d={},ip={} msg={}", query.getProductKey(), query.getDeviceName(), serverIp, rResponseEntity.getBody());
            return Pair.of(false, "设备消息发送失败");
        }
        
        return Pair.of(true, null);
    }
}
