package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleOtaFileService;
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
    private EleOtaFileService eleOtaFileService;
    
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

        if (EleOtaOperateRequest.TYPE_DOWNLOAD.equals(request.getOperateType())) {
            insertOrUpdateEleOtaFile(electricityCabinet, request);
            cacheDownloadResult(request, receiverMessage);
        }else if (EleOtaOperateRequest.TYPE_SYNC.equals(request.getOperateType())) {
            insertOrUpdateEleOtaFile(electricityCabinet, request);
            cacheSyncResult(request, receiverMessage);
        }



    }
    
    private void insertOrUpdateEleOtaFile(ElectricityCabinet electricityCabinet, EleOtaOperateRequest request) {
        EleOtaFile eleOtaFile = new EleOtaFile();
        eleOtaFile.setCoreSha256Value(request.getCoreSha256());
        eleOtaFile.setSubSha256Value(request.getSubSha256());
        eleOtaFile.setCoreName(request.getCoreName());
        eleOtaFile.setSubName(request.getSubName());

        EleOtaFile eleOtaFileFromDb = eleOtaFileService.queryByEid(electricityCabinet.getId());
        if (Objects.isNull(eleOtaFileFromDb)) {
            eleOtaFile.setElectricityCabinetId(electricityCabinet.getId());
            eleOtaFile.setCreateTime(System.currentTimeMillis());
            eleOtaFile.setUpdateTime(System.currentTimeMillis());
            eleOtaFileService.insert(eleOtaFile);
        } else {
            eleOtaFile.setUpdateTime(System.currentTimeMillis());
            eleOtaFileService.update(eleOtaFile);
        }
    }

    private void cacheDownloadResult(EleOtaOperateRequest request, ReceiverMessage receiverMessage){
        //操作回调的放在redis中
        if (Objects.nonNull(receiverMessage.getSuccess()) && "true".equalsIgnoreCase(receiverMessage.getSuccess())) {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + request.getOperateType() + ":" + receiverMessage.getSessionId(),queryDownloadStatus(request.getDownloadStatus()) , 30L,
                    TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + request.getOperateType() + ":" + receiverMessage.getSessionId(), "fail", 30L,
                    TimeUnit.SECONDS);
        }
    }

    private void cacheSyncResult(EleOtaOperateRequest request, ReceiverMessage receiverMessage){
        //操作回调的放在redis中
        if (Objects.nonNull(receiverMessage.getSuccess()) && "true".equalsIgnoreCase(receiverMessage.getSuccess())) {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + request.getOperateType() + ":" + receiverMessage.getSessionId(),"sync_success" , 30L,
                    TimeUnit.SECONDS);
        } else {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + request.getOperateType() + ":" + receiverMessage.getSessionId(), "sync_fail", 30L,
                    TimeUnit.SECONDS);
        }
    }

    private String queryDownloadStatus(Integer status) {
        String result = null;
        switch (status) {
            case 1: result = "download_fail"; break;
            case 2: result = "check_fail"; break;
            case 3: result = "check_success"; break;
            default: result = "fail"; break;
        }
        return  result;
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

    private String coreName;

    private String subName;
    
    private Long createTime;

    /**
     * 1--下载失败  2--下载成功但校验失败 3-- 下载成功且校验成功
     */
    private Integer downloadStatus;

    /**
     * 1--下载 2--同步 3-- 升级
     */
    private Integer operateType;

    public static final Integer TYPE_DOWNLOAD = 1;
    
    public static final Integer TYPE_SYNC = 2;
    
    public static final Integer TYPE_UPGRADE = 3;

    public static final Integer DOWNLOAD_STATUS_DOWNLOAD_FAIL = 1;

    public static final Integer DOWNLOAD_STATUS_CHECK_FAIL = 2;

    public static final Integer DOWNLOAD_STATUS_CHECK_SUCCESS = 3;
}
