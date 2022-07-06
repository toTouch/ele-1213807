package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 柜机核心板上报数据处理
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-07-06-11:21
 */
@Service(value = ElectricityIotConstant.NORMAL_CABINET_CORE_DATA_HANDLER)
@Slf4j
public class NormalCabinetCoreDataHandler extends AbstractElectricityIotHandler {

    @Autowired
    private EleCabinetCoreDataService eleCabinetCoreDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        EleCabinetCoreDataVO eleCabinetCoreDataVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleCabinetCoreDataVO.class);
        if (Objects.isNull(eleCabinetCoreDataVO)) {
            log.error("ele error! cabinetCoreData is null,productKey={},deviceName={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName());
            return;
        }

        this.atomicUpdateCabinetCoreData(eleCabinetCoreDataVO);
    }

    /**
     * 原子更新核心板上报数据
     */
    private void atomicUpdateCabinetCoreData(EleCabinetCoreDataVO eleCabinetCoreDataVO) {

        EleCabinetCoreData cabinetCoreData = EleCabinetCoreData.builder()
                .productKey(eleCabinetCoreDataVO.getProductKey()).
                        lockOpen(eleCabinetCoreDataVO.isLockOpen() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        smokeSensorOpen(eleCabinetCoreDataVO.isSmokeSensorOpen() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        lightOpen(eleCabinetCoreDataVO.isLightOpen() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        fanOpen(eleCabinetCoreDataVO.isFanOpen() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        extinguisherOpen(eleCabinetCoreDataVO.isExtinguisherOpen() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        v(eleCabinetCoreDataVO.getV()).
                        a(eleCabinetCoreDataVO.getA()).
                        power(eleCabinetCoreDataVO.getPower()).
                        powerFactor(eleCabinetCoreDataVO.getPowerFactor()).
                        activeElectricalEnergy(eleCabinetCoreDataVO.getActiveElectricalEnergy()).
                        waterPumpOpen(eleCabinetCoreDataVO.isWaterPumpOpen() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        waterLevelWarning(eleCabinetCoreDataVO.isWaterLevelWarning() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        temp(eleCabinetCoreDataVO.getTemp()).
                        humidity(eleCabinetCoreDataVO.getHumidity()).
                        waterLeachingWarning(eleCabinetCoreDataVO.isWaterLeachingWarning() ? EleCabinetCoreData.STSTUS_YES : EleCabinetCoreData.STSTUS_NO).
                        createTime(System.currentTimeMillis()).
                        updateTime(System.currentTimeMillis()).build();


        eleCabinetCoreDataService.atomicUpdateCabinetCoreData(cabinetCoreData);
    }


    @Data
    class EleCabinetCoreDataVO {
        private String productKey;
        private boolean isLockOpen;
        private boolean isSmokeSensorOpen;
        private boolean isLightOpen;
        private boolean isFanOpen;
        /**
         * 灭火器装置是否开启
         */
        private boolean isExtinguisherOpen;
        private double v;
        private double a;
        private double power;
        /**
         * 功率因数
         */
        private double powerFactor;
        /**
         * 有功电能
         */
        private double activeElectricalEnergy;
        private boolean isWaterPumpOpen;
        /**
         * 水箱水位是否报警
         */
        private boolean isWaterLevelWarning;
        /**
         * 温度
         */
        private double temp;
        /**
         * 湿度
         */
        private double humidity;
        /**
         * 柜内水浸状态
         */
        private boolean isWaterLeachingWarning;
    }

}
