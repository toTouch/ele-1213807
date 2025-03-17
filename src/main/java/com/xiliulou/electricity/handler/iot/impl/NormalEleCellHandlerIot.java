package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.dto.battery.BatteryLabelModifyDTO;
import com.xiliulou.electricity.entity.BoxOtherProperties;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;
import com.xiliulou.electricity.enums.LockTypeEnum;
import com.xiliulou.electricity.enums.battery.BatteryLabelEnum;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.ttl.TtlTraceIdSupport;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorServiceWrapper;
import com.xiliulou.electricity.ttl.TtlXllThreadPoolExecutorsSupport;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
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

    @Resource
    ElectricityCabinetBoxLockService boxLockService;


    private final TtlXllThreadPoolExecutorServiceWrapper serviceWrapper = TtlXllThreadPoolExecutorsSupport.get(
            XllThreadPoolExecutors.newFixedThreadPool("LOCK_BOX_CELL_EXECUTOR", 2, "lock-box-cell-executor"));



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
    
        Integer isBatteryExit = eleCellVo.getIs_battery_exit();
        if(Objects.nonNull(isBatteryExit)){
            electricityCabinetBox.setIsBatteryExit(isBatteryExit);
        }
    
        String version =
                Objects.isNull(eleCellVo.getVersion()) || "null".equalsIgnoreCase(eleCellVo.getVersion()) ? null
                        : eleCellVo.getVersion();
        if (Objects.nonNull(version)) {
            electricityCabinetBox.setVersion(version);
        }
        electricityCabinetBoxService.modifyCellByCellNo(electricityCabinetBox);

        //格挡禁用  保存禁用原因
        if (StringUtils.isNotEmpty(isForbidden) && Objects.equals(Integer.valueOf(isForbidden), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)) {
            BoxOtherProperties boxOtherProperties = new BoxOtherProperties();
            boxOtherProperties.setElectricityCabinetId(electricityCabinet.getId());
            boxOtherProperties.setCellNo(eleCellVo.getCell_no());
            boxOtherProperties.setDelFlag(BoxOtherProperties.DEL_NORMAL);
            boxOtherProperties.setLockReason(eleCellVo.getLockReason());
            boxOtherProperties.setLockStatusChangeTime(eleCellVo.getLockStatusChangeTime());
            boxOtherPropertiesService.insertOrUpdate(boxOtherProperties);

            // 保存锁仓列表
            saveLockBox(electricityCabinet, eleCellVo);
        }
        
        //启用格挡 移除格挡禁用原因
        if (StringUtils.isNotEmpty(isForbidden) && Objects.equals(Integer.valueOf(isForbidden), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE)) {
            BoxOtherProperties boxOtherProperties = new BoxOtherProperties();
            boxOtherProperties.setElectricityCabinetId(electricityCabinet.getId());
            boxOtherProperties.setCellNo(eleCellVo.getCell_no());
            boxOtherProperties.setDelFlag(BoxOtherProperties.DEL_NORMAL);
            boxOtherProperties.setLockReason(NumberConstant.ZERO);
            boxOtherProperties.setLockStatusChangeTime(eleCellVo.getLockStatusChangeTime());
            boxOtherProperties.setRemark(StringUtils.EMPTY);
            boxOtherPropertiesService.insertOrUpdate(boxOtherProperties);

            // 移除锁仓列表
            removeLockBox(electricityCabinet.getId(), eleCellVo);
            
            // 为保证锁仓sn在格挡启用后再删除，只能在此处清除锁仓sn，但是会产生多余的IO
            // 本需求又需要优先保证不影响现有功能，若想整合到上文的格挡更新中就需要提前查询格挡信息并做业务逻辑判断，既没有减少IO，又影响了现有逻辑，暂不整合
            modifyLabelAndClearLockSn(electricityCabinet.getId(), cellNo);
        }
    }

    private void removeLockBox(Integer eid, EleCellVO eleCellVo) {
        TtlTraceIdSupport.set(eleCellVo.getSessionId());
        try {
            serviceWrapper.execute(() -> {
                boxLockService.updateElectricityCabinetBoxLock(eid, eleCellVo.getCell_no());
            });
        } catch (Exception e) {
            log.error("removeLockBox Error! sessionId is {}", eleCellVo.getSessionId());
        } finally {
            TtlTraceIdSupport.clear();
        }
    }

    private void saveLockBox(ElectricityCabinet electricityCabinet, EleCellVO eleCellVo) {
        // 这里只保存锁仓类型为人工和系统的
        TtlTraceIdSupport.set(eleCellVo.getSessionId());
        try {
            if (LockTypeEnum.lockTypeCodeByDefined(eleCellVo.getLockType())) {
                serviceWrapper.execute(() -> {
                    if (StrUtil.isEmpty(eleCellVo.getCell_no())) {
                        log.error("SaveLockBox Error! cellNo is empty,sessionId is {}", eleCellVo.getSessionId());
                        return;
                    }
                    if (Objects.isNull(eleCellVo.getLockReason())) {
                        log.warn("SaveLockBox warn! lockReason is empty,sessionId is {}", eleCellVo.getSessionId());
                        return;
                    }
                    ElectricityCabinetBoxLock cabinetBoxLock = ElectricityCabinetBoxLock.builder().electricityCabinetId(electricityCabinet.getId()).cellNo(Integer.valueOf(eleCellVo.getCell_no()))
                            .lockType(eleCellVo.getLockType()).lockReason(eleCellVo.getLockReason()).lockStatusChangeTime(eleCellVo.getLockStatusChangeTime())
                             .sn(electricityCabinet.getSn())
                             .createTime(System.currentTimeMillis())
                            .deviceName(electricityCabinet.getDeviceName()).productKey(electricityCabinet.getProductKey())
                            .updateTime(System.currentTimeMillis()).tenantId(electricityCabinet.getTenantId())
                            .storeId(electricityCabinet.getStoreId()).franchiseeId(electricityCabinet.getFranchiseeId()).build();
                    boxLockService.insertElectricityCabinetBoxLock(cabinetBoxLock);
                });
            }
        } catch (Exception e) {
            log.error("saveLockBox Error! sessionId is {}", eleCellVo.getSessionId());
        } finally {
            TtlTraceIdSupport.clear();
        }
    }
    
    private void modifyLabelAndClearLockSn(Integer eid, String cellNo) {
        try {
            String traceId = MDC.get(CommonConstant.TRACE_ID);
            serviceWrapper.execute(() -> {
                MDC.put(CommonConstant.TRACE_ID, traceId);
                
                electricityCabinetBoxService.updateLockSnByEidAndCellNo(eid, cellNo, null);
                
                // 将电池锁定在仓的电池标签处理为闲置
                ElectricityCabinetBox box = electricityCabinetBoxService.queryByCellNo(eid, cellNo);
                if (Objects.isNull(box)) {
                    log.warn("MODIFY LABEL AND CLEAR LOCK SN WARN! box is null, eid={}, cellNo={}", eid, cellNo);
                    return;
                }
                
                ElectricityBattery electricityBattery = electricityBatteryService.queryBySnFromDb(box.getLockSn(), box.getTenantId());
                electricityBatteryService.syncModifyLabel(electricityBattery, box, new BatteryLabelModifyDTO(BatteryLabelEnum.UNUSED.getCode()), false);
            });
        } catch (Exception e) {
            log.error("MODIFY LABEL AND CLEAR LOCK SN ERROR! eid={}, cellNo={}", eid, cellNo, e);
        }
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
    
        /**
         * 在位检测 1：打开 0：关闭
         */
        private Integer is_battery_exit;

        private String sessionId;
    }
}




