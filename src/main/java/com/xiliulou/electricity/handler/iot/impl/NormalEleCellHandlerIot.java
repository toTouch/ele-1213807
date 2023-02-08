package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.BoxOtherProperties;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.BoxOtherPropertiesService;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Objects;


/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_ELE_CELL_HANDLER)
@Slf4j
public class NormalEleCellHandlerIot extends AbstractElectricityIotHandler {
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    BoxOtherPropertiesService boxOtherPropertiesService;


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        EleCellVO eleCellVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleCellVO.class);
        if (Objects.isNull(eleCellVo)) {
            log.error("ELE CELL REPORT ERROR! eleCellVo is null,sessionId={}", receiverMessage.getSessionId());
            return;
        }

        String cellNo = eleCellVo.getCell_no();
        if (StringUtils.isEmpty(cellNo)) {
            log.error("ELE CELL REPORT ERROR! cellNo is empty,sessionId={}", receiverMessage.getSessionId());
            return;
        }

        ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
        electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
        electricityCabinetBox.setCellNo(cellNo);
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());


        String isLock = eleCellVo.getIs_lock();
        if (StringUtils.isNotEmpty(isLock)) {
            electricityCabinetBox.setIsLock(Integer.valueOf(isLock));
        }

        String isFan = eleCellVo.getIs_fan();
        if (StringUtils.isNotEmpty(isFan)) {
            electricityCabinetBox.setIsFan(Integer.valueOf(isFan));
        }

        String temperature = eleCellVo.getTemperature();
        if (StringUtils.isNotEmpty(temperature)) {
            electricityCabinetBox.setTemperature(temperature);
        }

        String isHeat = eleCellVo.getIs_heat();
        if (StringUtils.isNotEmpty(isHeat)) {
            electricityCabinetBox.setIsHeat(Integer.valueOf(isHeat));
        }

        String isLight = eleCellVo.getIs_light();
        if (StringUtils.isNotEmpty(isLight)) {
            electricityCabinetBox.setIsLight(Integer.valueOf(isLight));
        }

        String isForbidden = eleCellVo.getIs_forbidden();
        if (StringUtils.isNotEmpty(isForbidden)) {
            electricityCabinetBox.setUsableStatus(Integer.valueOf(isForbidden));
        }

        Integer cellVoLockType = eleCellVo.getLockType();
        if (Objects.nonNull(cellVoLockType)) {
            electricityCabinetBox.setLockType(cellVoLockType);
        }
        
        String version =
                Objects.isNull(eleCellVo.getVersion()) || "null".equalsIgnoreCase(eleCellVo.getVersion()) ? null
                        : eleCellVo.getVersion();
        if (Objects.nonNull(version)) {
            electricityCabinetBox.setVersion(version);
        }
        electricityCabinetBoxService.modifyCellByCellNo(electricityCabinetBox);

        BoxOtherProperties boxOtherProperties = new BoxOtherProperties();
        boxOtherProperties.setElectricityCabinetId(electricityCabinet.getId());
        boxOtherProperties.setCellNo(eleCellVo.getCell_no());
        boxOtherProperties.setDelFlag(BoxOtherProperties.DEL_NORMAL);
        boxOtherProperties.setLockReason(Objects.isNull(eleCellVo.getLockReason()) ? -1 : eleCellVo.getLockReason());
        boxOtherProperties.setLockStatusChangeTime(Objects.isNull(eleCellVo.getLockStatusChangeTime()) ? 0L : eleCellVo.getLockStatusChangeTime());
        boxOtherProperties.setCreateTime(System.currentTimeMillis());
        boxOtherProperties.setUpdateTime(System.currentTimeMillis());
        boxOtherPropertiesService.insertOrUpdate(boxOtherProperties);
    }

    @Data
    class EleCellVO {
        //仓门号
        private String cell_no;
        //门锁状态
        private String is_lock;
        //风扇状态
        private String is_fan;
        //温度
        private String temperature;
        //加热状态
        private String is_heat;
        //指示灯状态
        private String is_light;
        //可用禁用
        private String is_forbidden;
        //锁仓类型 0--人为禁用 1--系统禁用 2--待检中
        private Integer lockType;
        /**
         * 锁仓原因
         */
        private Integer lockReason;
        /**
         * 锁仓/解锁时间
         */
        private Long lockStatusChangeTime;

        //子板版本号
        private String version;
    }
}




