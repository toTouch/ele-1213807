package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_BATTERY_HANDLER)
@Slf4j
public class NormalEleBatteryHandlerIot extends AbstractElectricityIotHandler {

    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    RedisService redisService;
    @Autowired
    FranchiseeBindElectricityBatteryService franchiseeBindElectricityBatteryService;
    @Autowired
    StoreService storeService;
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    @Autowired
    NotExistSnService notExistSnService;
    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;


    @Autowired
    EleCommonConfig eleCommonConfig;

    public static final String TERNARY_LITHIUM = "TERNARY_LITHIUM";
    public static final String IRON_LITHIUM = "IRON_LITHIUM";

    /**
     * 柜机ANCHI模式
     */
    public static final String ANCHI_BATTERY_PROTOCOL = "MULTI_V";


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        String sessionId = receiverMessage.getSessionId();

        EleBatteryVO eleBatteryVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVO.class);
        if (Objects.isNull(eleBatteryVO)) {
            log.error("ELE BATTERY REPORT ERROR! eleBatteryVO is null,sessionId={}", sessionId);
            return;
        }

        String cellNO = eleBatteryVO.getCellNo();
        if (StringUtils.isEmpty(cellNO)) {
            log.error("ELE BATTERY REPORT ERROR! cellNO is empty,cellNO={},sessionId={}", cellNO, sessionId);
            return;
        }

        ElectricityCabinetBox eleBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNO);
        if (Objects.isNull(eleBox)) {
            log.error("ELE BATTERY REPORT ERROR! no found electricityCabinetBox,electricityCabinetId={},sessionId={},cellNO={}", electricityCabinet.getId(), sessionId, cellNO);
            return;
        }

        Long reportTime = eleBatteryVO.getReportTime();
        //若上报时间为空  或者  上报时间小于上次上报时间，不处理
        if (Objects.nonNull(reportTime) && Objects.nonNull(eleBox.getReportTime()) && eleBox.getReportTime() >= reportTime) {
            log.error("ELE BATTERY REPORT ERROR! reportTime is empty,electricityCabinetId={},sessionId={}", electricityCabinet.getId(), sessionId);
            return;
        }

        Boolean existsBattery = eleBatteryVO.getExistsBattery();
        String batteryName = eleBatteryVO.getBatteryName();

        //存在电池但是电池名字没有上报
        if (Objects.nonNull(existsBattery) && existsBattery && StringUtils.isBlank(batteryName)) {
            log.error("ELE BATTERY REPORT ERROR! battery report illegal! existsBattery={},batteryName={},sessionId={}", eleBatteryVO.getExistsBattery(), eleBatteryVO.getBatteryName(), sessionId);
            return;
        }

        //不存在电池,上报了电池名字
        if (Objects.nonNull(existsBattery) && !existsBattery && StringUtils.isNotBlank(batteryName)) {
            log.warn("ELE BATTERY REPORT WARN! battery report illegal! battery name is exists,but existsBattery is false,batteryName={},sessionId={}", batteryName, sessionId);
            batteryName = null;
        }

        //处理电池名字为空
        if (StringUtils.isBlank(batteryName)) {
            this.handleBatteryNameIsBlank(eleBox, electricityCabinet, eleBatteryVO);
            log.warn("ELE BATTERY REPORT WARN！battery name is blank,sessionId={}", sessionId);
            return;
        }

        //检查本次上报的电池与格挡原来的电池是否一致
        this.checkBatteryNameIsEqual(eleBox, eleBatteryVO, sessionId);


        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
        if (Objects.isNull(electricityBattery)) {
            //保存未录入电池
            this.saveNotExistSn(batteryName, electricityCabinet, cellNO);
            log.warn("ELE BATTERY REPORT WARN!battery not input system,batteryName={},sessionId={}", batteryName, sessionId);
            return;
        }

//        NotExistSn oldNotExistSn = notExistSnService.queryByOther(batteryName, electricityCabinet.getId(), Integer.valueOf(cellNO));
//
//        if (Objects.isNull(electricityBattery)) {
//            //插入表
//            if (Objects.isNull(oldNotExistSn)) {
//                NotExistSn notExistSnOld = notExistSnService.queryByBatteryName(batteryName);
//                if (Objects.isNull(notExistSnOld)) {
//                    NotExistSn notExistSn = new NotExistSn();
//                    notExistSn.setEId(electricityCabinet.getId());
//                    notExistSn.setBatteryName(batteryName);
//                    notExistSn.setCellNo(Integer.valueOf(cellNO));
//                    notExistSn.setCreateTime(System.currentTimeMillis());
//                    notExistSn.setUpdateTime(System.currentTimeMillis());
//                    notExistSn.setTenantId(electricityCabinet.getTenantId());
//                    notExistSnService.insert(notExistSn);
//                } else {
//                    notExistSnOld.setEId(electricityCabinet.getId());
//                    notExistSnOld.setCellNo(Integer.valueOf(cellNO));
//                    notExistSnOld.setUpdateTime(System.currentTimeMillis());
//                    notExistSnService.update(notExistSnOld);
//                }
//            }
//            return;
//        }
//
//        //查询表中是否有电池
//        if (Objects.nonNull(oldNotExistSn)) {
//            oldNotExistSn.setDelFlag(NotExistSn.DEL_DEL);
//            oldNotExistSn.setUpdateTime(System.currentTimeMillis());
//            notExistSnService.update(oldNotExistSn);
//        }

        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.error("ELE BATTERY REPORT ERROR! tenantId is not equal,tenantId1={},tenantId2={},sessionId={}", electricityCabinet.getTenantId(), electricityBattery.getTenantId(), sessionId);
            return;
        }


        //更新电池信息
        ElectricityBattery updateBattery = new ElectricityBattery();
        updateBattery.setId(electricityBattery.getId());
        updateBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
        updateBattery.setElectricityCabinetId(electricityCabinet.getId());
        updateBattery.setElectricityCabinetName(electricityCabinet.getName());
        updateBattery.setLastDepositCellNo(cellNO);
        updateBattery.setUid(null);
        updateBattery.setBorrowExpireTime(null);
        updateBattery.setUpdateTime(System.currentTimeMillis());
        updateBattery.setHealthStatus(eleBatteryVO.getHealth());
        updateBattery.setChargeStatus(eleBatteryVO.getChargeStatus());


        //格挡信息
        ElectricityCabinetBox updateElectricityCabinetBox = new ElectricityCabinetBox();
        updateElectricityCabinetBox.setBatteryType(null);
        updateElectricityCabinetBox.setChargeV(eleBatteryVO.getChargeV());
        updateElectricityCabinetBox.setChargeA(eleBatteryVO.getChargeA());
        updateElectricityCabinetBox.setReportTime(reportTime);
        updateElectricityCabinetBox.setCellNo(cellNO);
        updateElectricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
        updateElectricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        updateElectricityCabinetBox.setSn(electricityBattery.getSn());
        updateElectricityCabinetBox.setBId(electricityBattery.getId());
        updateElectricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);

        //获取电池电量
        Double power = getBatteryPower(eleBatteryVO, electricityBattery, electricityCabinet, sessionId);
        if (Objects.nonNull(power)) {
            updateBattery.setPower(power * 100);
            updateElectricityCabinetBox.setPower(power * 100);
        }

        //获取电池型号
        if (Objects.nonNull(eleBatteryVO.getIsMultiBatteryModel()) && eleBatteryVO.getIsMultiBatteryModel()) {
            String batteryModel = parseBatteryNameAcquireBatteryModel(batteryName);
            updateBattery.setModel(batteryModel);
            updateElectricityCabinetBox.setBatteryType(batteryModel);
        }

        //检查电池所属加盟商
        this.checkBatteryFranchisee(electricityCabinet, electricityBattery, updateElectricityCabinetBox, sessionId);


        //保存电池上报其他信息
        this.saveBatteryOtherProperties(eleBatteryVO, batteryName);
        //更新电池
        electricityBatteryService.updateByOrder(updateBattery);
        //更新格挡
        electricityCabinetBoxService.modifyByCellNo(updateElectricityCabinetBox);
    }

    private void handleBatteryNameIsBlank(ElectricityCabinetBox eleBox, ElectricityCabinet electricityCabinet, EleBatteryVO eleBatteryVO) {
        ElectricityCabinetBox updateElectricityCabinetBox = new ElectricityCabinetBox();
        updateElectricityCabinetBox.setBatteryType(null);
        updateElectricityCabinetBox.setChargeV(eleBatteryVO.getChargeV());
        updateElectricityCabinetBox.setChargeA(eleBatteryVO.getChargeA());
        updateElectricityCabinetBox.setReportTime(eleBatteryVO.getReportTime());
        updateElectricityCabinetBox.setCellNo(eleBatteryVO.getCellNo());
        updateElectricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
        updateElectricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        updateElectricityCabinetBox.setBId(null);
        updateElectricityCabinetBox.setSn(null);
        updateElectricityCabinetBox.setPower(null);
        updateElectricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
        electricityCabinetBoxService.modifyByCellNo(updateElectricityCabinetBox);

        //原来仓门是否有电池
        if (StringUtils.isNotBlank(eleBox.getSn())) {
            if (eleBox.getSn().contains("UNKNOW")) {
                eleBox.setSn(eleBox.getSn().substring(6));
            }

            //更新原仓门中的电池
            ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(eleBox.getSn());
            if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS)) {
                ElectricityBattery updateBattery = new ElectricityBattery();
                updateBattery.setId(electricityBattery.getId());
                updateBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
                updateBattery.setElectricityCabinetId(null);
                updateBattery.setElectricityCabinetName(null);
                updateBattery.setUid(null);
                updateBattery.setUpdateTime(System.currentTimeMillis());
                electricityBatteryService.updateByOrder(updateBattery);
            }
        }
    }

    /**
     * 检查本次上报的电池与格挡原来的电池是否一致
     *
     * @param eleBox
     * @param eleBatteryVO
     * @param sessionId
     * @return
     */
    private void checkBatteryNameIsEqual(ElectricityCabinetBox eleBox, EleBatteryVO eleBatteryVO, String sessionId) {
        if (StringUtils.isBlank(eleBox.getSn()) || eleBox.getSn().equals(eleBatteryVO.getBatteryName())) {
            return;
        }

        //更新原仓门中的电池状态为异常取走
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(eleBox.getSn());
        if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS)) {
            ElectricityBattery updateBattery = new ElectricityBattery();
            updateBattery.setId(electricityBattery.getId());
            updateBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
            updateBattery.setElectricityCabinetId(null);
            updateBattery.setElectricityCabinetName(null);
            updateBattery.setUid(null);
            updateBattery.setUpdateTime(System.currentTimeMillis());

            log.info("ELE BATTERY REPORT INFO! current batteryName not equals original batteryName,currentBatteryName={},originalBatteryName={},sessionId={}", eleBatteryVO.getBatteryName(), eleBox.getSn(), sessionId);
            electricityBatteryService.updateByOrder(updateBattery);
        }
    }


    /**
     * 判断电池是否录入
     *
     * @param batteryName
     * @param electricityCabinet
     * @param cellNO
     * @return
     */
    private void saveNotExistSn(String batteryName, ElectricityCabinet electricityCabinet, String cellNO) {

        NotExistSn notExistSnOld = notExistSnService.queryByBatteryName(batteryName);
        if (Objects.isNull(notExistSnOld)) {
            NotExistSn notExistSn = new NotExistSn();
            notExistSn.setEId(electricityCabinet.getId());
            notExistSn.setBatteryName(batteryName);
            notExistSn.setCellNo(Integer.valueOf(cellNO));
            notExistSn.setCreateTime(System.currentTimeMillis());
            notExistSn.setUpdateTime(System.currentTimeMillis());
            notExistSn.setTenantId(electricityCabinet.getTenantId());
            notExistSnService.insert(notExistSn);
        } else {
            notExistSnOld.setEId(electricityCabinet.getId());
            notExistSnOld.setCellNo(Integer.valueOf(cellNO));
            notExistSnOld.setUpdateTime(System.currentTimeMillis());
            notExistSnService.update(notExistSnOld);
        }
    }

    /**
     * 获取电池电量
     *
     * @param eleBatteryVO
     * @param electricityBattery
     * @param electricityCabinet
     * @param sessionId
     * @return
     */
    private Double getBatteryPower(EleBatteryVO eleBatteryVO, ElectricityBattery electricityBattery, ElectricityCabinet electricityCabinet, String sessionId) {
        Double power = eleBatteryVO.getPower();
        //柜机模式
        String applicationMode = null;


        try {
            ElectricityCabinetOtherSetting eleOtherSetting = redisService.getWithHash(CacheConstant.OTHER_CONFIG_CACHE + electricityCabinet.getId(), ElectricityCabinetOtherSetting.class);
            if (Objects.nonNull(eleOtherSetting)) {
                applicationMode = eleOtherSetting.getApplicationMode();
            }
        } catch (Exception e) {
            log.error("ELE BATTERY REPORT ERROR! applicationMode parsing failed,electricityCabinetId={},sessionId={}", electricityCabinet.getId(), sessionId);
        }


        /**
         * 1.如果柜机模式为 MULTI_V，不管nacos是否开启电量变化检测，都不进行电量变化太大检测
         */
        if (StringUtils.isNotBlank(applicationMode) && ANCHI_BATTERY_PROTOCOL.equals(applicationMode)) {
            log.info("ELE BATTERY REPORT INFO! applicationMode:MULTI_V,report power={},sessionId={}", eleBatteryVO.getPower(), sessionId);
            return power;
        }


        /**
         * 2.如果柜机模式为空，或者柜机模式为其他，根据nacos配置判断是否需要电量变化太大检测
         */
        //获取nacos电量检测配置
        Integer isBatteryReportCheck = eleCommonConfig.getBatteryReportCheck();
        if (Objects.nonNull(isBatteryReportCheck) && Objects.equals(isBatteryReportCheck, EleCommonConfig.OPEN_BATTERY_REPORT_CHECK)) {
            if (Objects.nonNull(electricityBattery.getPower()) && Objects.nonNull(power) && Math.abs(electricityBattery.getPower() - (power * 100)) >= 50 && Math.abs(electricityBattery.getPower() - (power * 100)) != 100) {
                //如果开启电量变化检测，并且本次上报电量和上次上报电量相差超过50，则power仍设置为原来的值
                power = electricityBattery.getPower() / 100.0;

                log.warn("ELE BATTERY REPORT WARN! battery power is changing too much,reportPower={},originalPower={},sessionId={}", eleBatteryVO.getPower(), power, sessionId);
                return power;
            }
        }

        return power;
    }

    /**
     * 检查电池是否属于当前柜机的加盟商
     */
    private void checkBatteryFranchisee(ElectricityCabinet electricityCabinet, ElectricityBattery electricityBattery, ElectricityCabinetBox updateElectricityCabinetBox, String sessionId) {
        //查电池所属加盟商
        FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryId(electricityBattery.getId());
        if (Objects.isNull(franchiseeBindElectricityBattery)) {
            log.error("ELE BATTERY REPORT ERROR! battery not bind franchisee,electricityBatteryId={},sessionId={}", electricityBattery.getId(), sessionId);
            updateElectricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
            return;
        }

        // 查换电柜所属加盟商
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (!Objects.equals(store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId().longValue())) {
            log.error("ELE BATTERY REPORT ERROR! franchisee is not equal,franchiseeId1={},franchiseeId2={},sessionId={}", store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId(), sessionId);
            updateElectricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
        }
    }

    private void saveBatteryOtherProperties(EleBatteryVO eleBatteryVO, String batteryName) {
        if (Objects.nonNull(eleBatteryVO.getHasOtherAttr()) && eleBatteryVO.getHasOtherAttr()) {
            BatteryOtherPropertiesQuery batteryOtherPropertiesQuery = eleBatteryVO.getBatteryOtherProperties();
            BatteryOtherProperties batteryOtherProperties = new BatteryOtherProperties();
            BeanUtils.copyProperties(batteryOtherPropertiesQuery, batteryOtherProperties);
            batteryOtherProperties.setBatteryName(batteryName);
            batteryOtherProperties.setBatteryCoreVList(JsonUtil.toJson(batteryOtherPropertiesQuery.getBatteryCoreVList()));
            batteryOtherPropertiesService.insertOrUpdate(batteryOtherProperties);
        }
    }

    public static String parseBatteryNameAcquireBatteryModel(String batteryName) {
        if (StringUtils.isEmpty(batteryName) || batteryName.length() < 11) {
            return "";
        }

        StringBuilder modelName = new StringBuilder("B_");
        char[] batteryChars = batteryName.toCharArray();

        //获取电压
        String chargeV = split(batteryChars, 4, 6);
        modelName.append(chargeV).append("V").append("_");

        //获取材料体系
        char material = batteryChars[2];
        if (material == '1') {
            modelName.append(IRON_LITHIUM).append("_");
        } else {
            modelName.append(TERNARY_LITHIUM).append("_");
        }

        modelName.append(split(batteryChars, 9, 11));
        return modelName.toString();
    }

    private static String split(char[] strArray, int beginIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            stringBuilder.append(strArray[i]);
        }
        return stringBuilder.toString();
    }

    @Data
    class EleBatteryVO {
        private String batteryName;
        //电量
        private Double power;
        //健康状态
        private Integer health;
        //充电状态
        private Integer chargeStatus;
        //cellNo
        private String cellNo;
        //reportTime
        private Long reportTime;
        //充电器电压
        private Double chargeV;
        //充电器电流
        private Double chargeA;

        private Boolean existsBattery;

        private Boolean isMultiBatteryModel;

        private Boolean hasOtherAttr;

        private BatteryOtherPropertiesQuery batteryOtherProperties;

    }
}




