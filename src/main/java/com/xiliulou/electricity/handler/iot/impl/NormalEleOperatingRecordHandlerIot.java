package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetPhysicsOperRecord;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.ElectricityCabinetPhysicsOperRecordService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.StringTokenizer;

/**
 * @author zgw
 * @date 2022/8/16 14:06
 * @mood
 */
@Service(value= ElectricityIotConstant.NORMAL_CUPBOARD_OPERATING_RECORD_HANDLER)
@Slf4j
public class NormalEleOperatingRecordHandlerIot extends AbstractElectricityIotHandler {

    @Autowired
    ElectricityCabinetPhysicsOperRecordService electricityCabinetPhysicsOperRecordService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        CupboardOperatingRecordRequest request = JsonUtil.fromJson(receiverMessage.getOriginContent(), CupboardOperatingRecordRequest.class);
        if(Objects.isNull(request)) {
            log.warn("CUPBOARD OPERATING RECORD ERROR! parse CupboardOperatingRecordRequest error! sessionId={}, productKey={},deviceName={}",receiverMessage.getSessionId(), receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return;
        }
    
        if(StrUtil.isNotBlank(request.getCellNo())){
            StringTokenizer stringTokenizer = new StringTokenizer(request.getCellNo(), ",");
            while (stringTokenizer.hasMoreElements()) {
                createPhysicsOperRecord(electricityCabinet, request, stringTokenizer.nextToken());
            }
            return;
        }
    
        createPhysicsOperRecord(electricityCabinet, request, null);
    }
    
    public void createPhysicsOperRecord(ElectricityCabinet electricityCabinet, CupboardOperatingRecordRequest request, String cellNo){
        ElectricityCabinetPhysicsOperRecord electricityCabinetPhysicsOperRecord = new ElectricityCabinetPhysicsOperRecord();
        electricityCabinetPhysicsOperRecord.setElectricityCabinetId(electricityCabinet.getId());
        electricityCabinetPhysicsOperRecord.setCreateTime(System.currentTimeMillis());
        electricityCabinetPhysicsOperRecord.setCommand(request.getIoTMsgType());
        electricityCabinetPhysicsOperRecord.setCellNo(cellNo);
        electricityCabinetPhysicsOperRecord.setStatus(Objects.isNull(request.getResult()) || !request.getResult()
                ? CupboardOperatingRecordRequest.RESULT_FAIL : CupboardOperatingRecordRequest.RESULT_SUCCESS);
        electricityCabinetPhysicsOperRecord.setMsg(request.getOperateMsg());
        electricityCabinetPhysicsOperRecord.setUid(request.getUid());
        electricityCabinetPhysicsOperRecord.setUserName(request.getUsername());
        electricityCabinetPhysicsOperRecord.setOperateType(request.getOperateType());
        electricityCabinetPhysicsOperRecordService.insert(electricityCabinetPhysicsOperRecord);
    }

}

@Data
class CupboardOperatingRecordRequest {
    /**
     * 消息类型
     */
    private  String type;
    private String sessionId;
    private String productKey;
    private String devicesName;
    /**
     *操作类型 1--命令下发 2--柜机操作
     */
    private Integer operateType;
    /**
     * 操作信息
     */
    private String operateMsg;
    /**
     * 操作命令
     */
    private String ioTMsgType;
    /**
     * 操作结果
     */
    private Boolean result;
    
    private String cellNo;
    private Long createTime;
    private Long uid;
    private String username;

    public static final Integer TYPE_IOT = 1;
    public static final Integer TYPE_LOAD = 2;

    public static final Integer RESULT_SUCCESS = 1;
    public static final Integer RESULT_FAIL = 2;
}
