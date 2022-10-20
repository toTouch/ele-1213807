package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleOtaFile;
import com.xiliulou.electricity.entity.EleOtaUpgrade;
import com.xiliulou.electricity.entity.EleOtaUpgradeHistory;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleOtaFileService;
import com.xiliulou.electricity.service.EleOtaUpgradeHistoryService;
import com.xiliulou.electricity.service.EleOtaUpgradeService;
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
    
    @Autowired
    EleOtaUpgradeService eleOtaUpgradeService;
    
    @Autowired
    EleOtaUpgradeHistoryService eleOtaUpgradeHistoryService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        if (StrUtil.isBlank(receiverMessage.getOriginContent())) {
            redisService.set(CacheConstant.OTA_OPERATE_CACHE + receiverMessage.getSessionId(), "操作失败", 30L,
                    TimeUnit.SECONDS);
        }
        
        EleOtaOperateRequest request = JsonUtil
                .fromJson(receiverMessage.getOriginContent(), EleOtaOperateRequest.class);
        if (Objects.isNull(request)) {
            log.warn(
                    "ELE CHECK OTA FILE SHA256 VALUE ERROR! parse EleOtaFileSha256Request error! sessionId={}, productKey={},deviceName={}",
                    receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return;
        }
    
        String sessionId = request.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("no sessionId,{}", receiverMessage.getOriginContent());
            return;
        }
        
        if (EleOtaOperateRequest.TYPE_DOWNLOAD.equals(request.getOperateType()) || EleOtaOperateRequest.TYPE_SYNC
                .equals(request.getOperateType())) {
            insertOrUpdateEleOtaFile(electricityCabinet, request);
            //操作回调的放在redis中
            if (Objects.nonNull(request.getSuccess()) && "true".equalsIgnoreCase(request.getSuccess())) {
                redisService.set(CacheConstant.OTA_OPERATE_CACHE + sessionId, "ok", 30L, TimeUnit.SECONDS);
            } else {
                redisService.set(CacheConstant.OTA_OPERATE_CACHE + sessionId, request.getMsg(), 30L, TimeUnit.SECONDS);
            }
            return;
        }

        if(Objects.isNull(request.getCellNo())) {
            request.setCellNo(0);
        }
    
        updateEleOtaUpgrade(electricityCabinet, receiverMessage, request);
    }
    
    private void updateEleOtaUpgrade(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage,
            EleOtaOperateRequest request) {
        Integer type = Objects.equals(request.getCellNo(), 0) ? EleOtaUpgrade.TYPE_CORE : EleOtaUpgrade.TYPE_SUB;
        EleOtaUpgrade eleOtaUpgradeFromDb = eleOtaUpgradeService
                .queryByEidAndCellNo(electricityCabinet.getId(), request.getCellNo(), type);
        if (Objects.isNull(eleOtaUpgradeFromDb)) {
            log.error("OTA UPGRADE ERROR! eleOtaUpgrade is null error! session={}, eid={}, cid={}",
                    receiverMessage.getSessionId(), electricityCabinet.getId(), request.getCellNo());
            return;
        }
        
        EleOtaUpgrade updateEleOtaUpgrade = new EleOtaUpgrade();
        updateEleOtaUpgrade.setId(eleOtaUpgradeFromDb.getId());
        updateEleOtaUpgrade.setStatus(queryStatus(request.getStatus()));
        updateEleOtaUpgrade.setUpdateTime(System.currentTimeMillis());
        eleOtaUpgradeService.update(eleOtaUpgradeFromDb);
    
        EleOtaUpgradeHistory eleOtaUpgradeHistory = eleOtaUpgradeHistoryService
                .queryByCellNoAndSessionId(electricityCabinet.getId(), request.getCellNo(), request.getSessionId(),
                        type);
        if (Objects.isNull(eleOtaUpgradeHistory)) {
            log.error("OTA UPGRADE ERROR! eleOtaUpgradeHistory is null error! session={}, eid={}, cid={}",
                    receiverMessage.getSessionId(), electricityCabinet.getId(), request.getCellNo());
            return;
        }
        
        EleOtaUpgradeHistory updateEleOtaUpgradeHistory = new EleOtaUpgradeHistory();
        updateEleOtaUpgradeHistory.setId(eleOtaUpgradeHistory.getId());
        updateEleOtaUpgrade.setStatus(queryStatus(request.getStatus()));
        updateEleOtaUpgradeHistory.setUpgradeSha256Value(
                Objects.equals(type, EleOtaUpgrade.TYPE_CORE) ? request.getCoreSha256() : request.getSubSha256());
        updateEleOtaUpgradeHistory.setErrMsg(request.getMsg());
        if (Objects.equals(EleOtaOperateRequest.STATUS_UPGRADING, request.getStatus())) {
            updateEleOtaUpgradeHistory.setUpgradeTime(request.getCreateTime());
        } else if (Objects.equals(EleOtaOperateRequest.STATUS_UPGRADE_SUCCESS, request.getStatus()) || Objects
                .equals(EleOtaOperateRequest.STATUS_UPGRADE_FAIL, request.getStatus())) {
            updateEleOtaUpgradeHistory.setFinishTime(request.getCreateTime());
        }
        eleOtaUpgradeHistoryService.update(updateEleOtaUpgradeHistory);
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
    
    private String queryStatus(Integer status) {
        String result;
        switch (status) {
            case 6:
                result = EleOtaUpgrade.STATUS_UPGRADING;
                break;
            case 7:
                result = EleOtaUpgrade.STATUS_UPGRADE_SUCCESS;
                break;
            case 8:
                result = EleOtaUpgrade.STATUS_UPGRADE_FAIL;
                break;
            default:
                result = null;
                break;
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
     * 1--下载失败  2--下载成功但校验失败 3-- 下载成功且校验成功 4--同步成功 5--同步失败 6 --正在升级 7--升级成功 8--升级失败
     */
    private Integer status;

    /**
     * 1--下载 2--同步 3-- 升级
     */
    private Integer operateType;
    
    private Integer cellNo;
    
    private String msg;
    
    private String success;

    public static final Integer TYPE_DOWNLOAD = 1;
    
    public static final Integer TYPE_SYNC = 2;
    
    public static final Integer TYPE_UPGRADE = 3;
    
    public static final Integer STATUS_DOWNLOAD_FAIL = 1;
    
    public static final Integer STATUS_CHECK_FAIL = 2;
    
    public static final Integer STATUS_CHECK_SUCCESS = 3;
    
    public static final Integer STATUS_SYNC_SUCCESS = 4;
    
    public static final Integer STATUS_SYNC_FAIL = 5;
    
    public static final Integer STATUS_UPGRADING = 6;
    
    public static final Integer STATUS_UPGRADE_SUCCESS = 7;
    
    public static final Integer STATUS_UPGRADE_FAIL = 8;
}
