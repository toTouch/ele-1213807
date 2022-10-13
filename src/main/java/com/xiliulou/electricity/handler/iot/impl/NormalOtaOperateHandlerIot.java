package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.OtaFileEleSha256;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.OtaFileEleSha256Service;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zgw
 * @date 2022/10/12 19:30
 * @mood
 */
@Service(value = ElectricityIotConstant.NORMAL_OTA_OPERATE_HANDLER)
@Slf4j
public class NormalOtaOperateHandlerIot extends AbstractElectricityIotHandler {
    
    @Autowired
    private OtaFileEleSha256Service otaFileEleSha256Service;
    
    @Autowired
    RedisService redisService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return;
        }
        
        EleOtaOperateRequest request = JsonUtil
                .fromJson(receiverMessage.getOriginContent(), EleOtaOperateRequest.class);
        if (Objects.isNull(request)) {
            log.warn(
                    "ELE CHECK OTA FILE SHA256 VALUE ERROR! parse EleOtaFileSha256Request error! sessionId={}, productKey={},deviceName={}",
                    receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return;
        }
        
        if (EleOtaOperateRequest.TYPE_SYNC.equals(request.getOperateType())) {
            insertOrUpdateOtaFileEleSha256(electricityCabinet, request);
        }
        
        //操作回调的放在redis中
        if (Objects.nonNull(receiverMessage.getSuccess()) && "true".equalsIgnoreCase(receiverMessage.getSuccess())) {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + request.getOperateType() + ":" + sessionId, "true", 30L,
                    TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + request.getOperateType() + ":" + sessionId, "false", 30L,
                    TimeUnit.SECONDS);
        }
    }
    
    private void insertOrUpdateOtaFileEleSha256(ElectricityCabinet electricityCabinet, EleOtaOperateRequest request) {
        OtaFileEleSha256 otaFileEleSha256 = new OtaFileEleSha256();
        otaFileEleSha256.setCoreSha256Value(request.getCoreSha256());
        otaFileEleSha256.setSubSha256Value(request.getSubSha256());
        
        OtaFileEleSha256 otaFileEleSha256FromDb = otaFileEleSha256Service.queryByEid(electricityCabinet.getId());
        if (Objects.isNull(otaFileEleSha256FromDb)) {
            otaFileEleSha256.setElectricityCabinetId(electricityCabinet.getId());
            otaFileEleSha256.setCreateTime(System.currentTimeMillis());
            otaFileEleSha256.setUpdateTime(System.currentTimeMillis());
            otaFileEleSha256Service.insert(otaFileEleSha256);
        } else {
            otaFileEleSha256.setUpdateTime(System.currentTimeMillis());
            otaFileEleSha256Service.update(otaFileEleSha256);
        }
    }
}

@Data
class EleOtaOperateRequest {
    
    /**
     * 消息类型
     */
    private String type;
    
    private String sessionId;
    
    private String productKey;
    
    private String devicesName;
    
    private String coreSha256;
    
    private String subSha256;
    
    private Long createTime;
    
    /**
     * 1--下载 2--同步 3-- 升级
     */
    private Integer operateType;
    
    public static final Integer TYPE_DOWNLOAD = 1;
    
    public static final Integer TYPE_SYNC = 2;
    
    public static final Integer TYPE_UPGRADE = 3;
}
