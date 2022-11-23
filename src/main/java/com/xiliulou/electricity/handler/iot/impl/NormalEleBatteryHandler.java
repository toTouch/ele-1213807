package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSON;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.constant.MqConstant;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
import com.xiliulou.iot.entity.ReceiverMessage;
import com.xiliulou.mq.service.RocketMqService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author: zzlong
 * @Date: 2022/08/12 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_BATTERY_HANDLER)
@Slf4j
public class NormalEleBatteryHandler extends AbstractElectricityIotHandler {

    ExecutorService voltageCurrentExecutorService = XllThreadPoolExecutors.newFixedThreadPool("eleSaveVoltageCurrent", 1, "ele_Save_Voltage_Current");

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
    ElectricityCabinetModelService electricityCabinetModelService;
    @Autowired
    RocketMqService rocketMqService;
    @Autowired
    MaintenanceUserNotifyConfigService maintenanceUserNotifyConfigService;

    @Autowired
    ClickHouseService clickHouseService;
    
    private static DateTimeFormatter formatter=DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN);
    
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
            log.error("ELE BATTERY REPORT ERROR! cellNO is empty,sessionId={}", sessionId);
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
            return;
        }

        //检查本次上报的电池与格挡原来的电池是否一致
        this.checkBatteryNameIsEqual(eleBox, eleBatteryVO, sessionId);


        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName, electricityCabinet.getTenantId());
        
        //检查电池是否在未录入表中
        NotExistSn notExistSn = notExistSnService.queryByBatteryName(batteryName);
        //未录入
        if (Objects.isNull(electricityBattery)) {
            this.insertOrUpdateNotExistSn(notExistSn, batteryName, electricityCabinet, cellNO);
            return;
        }
        //已录入
        if (Objects.nonNull(electricityBattery) && Objects.nonNull(notExistSn)) {
            notExistSnService.deleteNotExistSn(notExistSn);
        }

        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.error("ELE BATTERY REPORT ERROR! tenantId is not equal,tenantId1={},tenantId2={},sessionId={}", electricityCabinet.getTenantId(), electricityBattery.getTenantId(), sessionId);
            return;
        }

        //保存电池电压电流&充电器电压电流
        this.checkBatteryAndCharger(electricityCabinet,eleBox,electricityBattery,eleBatteryVO,sessionId);

        //获取电池电量
        Double power = getBatteryPower(eleBatteryVO, electricityBattery, electricityCabinet, sessionId);

        ElectricityBattery updateBattery = this.buildElectricityBattery(eleBatteryVO, electricityBattery, electricityCabinet, power);

        ElectricityCabinetBox updateElectricityCabinetBox = this.buildElectricityCabinetBox(eleBatteryVO, electricityBattery, electricityCabinet, power);


        //检查电池所属加盟商
        this.checkBatteryFranchisee(electricityCabinet, electricityBattery, updateElectricityCabinetBox, sessionId);

        //更新电池
        electricityBatteryService.updateBatteryStatus(updateBattery);

        //更新格挡
        electricityCabinetBoxService.modifyByCellNo(updateElectricityCabinetBox);

        //保存电池上报其他信息
        this.saveBatteryOtherProperties(eleBatteryVO, batteryName);
        
        //检查柜机电池是否满仓
//        this.checkElectricityCabinetBatteryFull(electricityCabinet);
    }

    private ElectricityBattery buildElectricityBattery(EleBatteryVO eleBatteryVO, ElectricityBattery electricityBattery, ElectricityCabinet electricityCabinet, Double power) {
        ElectricityBattery battery = new ElectricityBattery();
        battery.setId(electricityBattery.getId());
        battery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE);
        battery.setElectricityCabinetId(electricityCabinet.getId());
        battery.setElectricityCabinetName(electricityCabinet.getName());
//        battery.setLastDepositCellNo(eleBatteryVO.getCellNo());
//        battery.setUid(null);
        battery.setBorrowExpireTime(null);
        battery.setUpdateTime(System.currentTimeMillis());
        battery.setHealthStatus(eleBatteryVO.getHealth());
        battery.setChargeStatus(eleBatteryVO.getChargeStatus());

        if (Objects.nonNull(power)) {
            battery.setPower(power * 100);
        }

        //获取电池型号
        if (Objects.nonNull(eleBatteryVO.getIsMultiBatteryModel()) && eleBatteryVO.getIsMultiBatteryModel()) {
            String batteryModel = parseBatteryNameAcquireBatteryModel(eleBatteryVO.getBatteryName());
            battery.setModel(batteryModel);
        }

        return battery;
    }

    private ElectricityCabinetBox buildElectricityCabinetBox(EleBatteryVO eleBatteryVO, ElectricityBattery electricityBattery, ElectricityCabinet electricityCabinet, Double power) {
        ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
        electricityCabinetBox.setBatteryType(null);
        electricityCabinetBox.setChargeV(eleBatteryVO.getChargeV());
        electricityCabinetBox.setChargeA(eleBatteryVO.getChargeA());
        electricityCabinetBox.setReportTime(eleBatteryVO.getReportTime());
        electricityCabinetBox.setCellNo(eleBatteryVO.getCellNo());
        electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBox.setSn(electricityBattery.getSn());
        electricityCabinetBox.setBId(electricityBattery.getId());
        electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);

        if (Objects.nonNull(power)) {
            electricityCabinetBox.setPower(power * 100);
        }

        //获取电池型号
        if (Objects.nonNull(eleBatteryVO.getIsMultiBatteryModel()) && eleBatteryVO.getIsMultiBatteryModel()) {
            String batteryModel = parseBatteryNameAcquireBatteryModel(eleBatteryVO.getBatteryName());
            electricityCabinetBox.setBatteryType(batteryModel);
        }

        return electricityCabinetBox;
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

        if (StringUtils.isBlank(eleBox.getSn())) {
            return;
        }

        //原来仓门有电池
        if (eleBox.getSn().contains("UNKNOW")) {
            eleBox.setSn(eleBox.getSn().substring(6));
        }

        //更新原仓门中的电池
        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(eleBox.getSn());
        //            if (Objects.nonNull(electricityBattery) && !Objects.equals(electricityBattery.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE)) {
        //                ElectricityBattery updateBattery = new ElectricityBattery();
        //                updateBattery.setId(electricityBattery.getId());
        //                updateBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
        //                updateBattery.setElectricityCabinetId(null);
        //                updateBattery.setElectricityCabinetName(null);
        ////                updateBattery.setUid(null);
        //                updateBattery.setUpdateTime(System.currentTimeMillis());
        //                electricityBatteryService.updateByOrder(updateBattery);
        //            }
        if (Objects.nonNull(electricityBattery)) {
            ElectricityBattery updateBattery = new ElectricityBattery();
            updateBattery.setId(electricityBattery.getId());
            updateBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
            updateBattery.setElectricityCabinetId(null);
            updateBattery.setElectricityCabinetName(null);
            updateBattery.setBorrowExpireTime(null);
//            updateBattery.setUid(null);
            updateBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.updateBatteryStatus(updateBattery);
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
        if (Objects.nonNull(electricityBattery)) {
            ElectricityBattery updateBattery = new ElectricityBattery();
            updateBattery.setId(electricityBattery.getId());
            updateBattery.setPhysicsStatus(ElectricityBattery.PHYSICS_STATUS_NOT_WARE_HOUSE);
            updateBattery.setElectricityCabinetId(null);
            updateBattery.setElectricityCabinetName(null);
//            updateBattery.setUid(null);
            updateBattery.setBorrowExpireTime(null);
            updateBattery.setUpdateTime(System.currentTimeMillis());

            electricityBatteryService.updateBatteryStatus(updateBattery);
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
    private void insertOrUpdateNotExistSn(NotExistSn notExistSnOld,String batteryName, ElectricityCabinet electricityCabinet, String cellNO) {

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
            ElectricityCabinetOtherSetting eleOtherSetting = redisService.getWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinet.getId(), ElectricityCabinetOtherSetting.class);
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
         * 2.如果柜机模式为空，或者柜机模式为其他  并且电池上一次在仓，检查电量变化是否太大
         */
        if (Objects.nonNull(electricityBattery.getPower())
                && Objects.equals(electricityBattery.getPhysicsStatus(), ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)
                && Objects.nonNull(power)
//                && electricityBattery.getPower() != 0 //排除刚录入的电池，新录入的电池电量为0
                && (electricityBattery.getPower() - (power * 100)) >= 50) {

            //如果开启电量变化检测，并且本次上报电量和上次上报电量相差超过50，则power仍设置为原来的值
            power = electricityBattery.getPower() / 100.0;

            log.warn("ELE BATTERY REPORT WARN! battery power is changing too much,reportPower={},originalPower={},sessionId={}",
                    eleBatteryVO.getPower(), power, sessionId);
            return power;
        }

        return power;
    }

    /**
     * 检查电池是否属于当前柜机的加盟商
     */
    private void checkBatteryFranchisee(ElectricityCabinet electricityCabinet, ElectricityBattery electricityBattery, ElectricityCabinetBox updateElectricityCabinetBox, String sessionId) {
        //查电池所属加盟商
//        FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryId(electricityBattery.getId());
//        if (Objects.isNull(franchiseeBindElectricityBattery)) {
//            log.error("ELE BATTERY REPORT ERROR! battery not bind franchisee,electricityBatteryId={},sessionId={}", electricityBattery.getId(), sessionId);
//            updateElectricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
//            return;
//        }

        // 查换电柜所属加盟商
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (!Objects.equals(store.getFranchiseeId(), electricityBattery.getFranchiseeId())) {
            log.error("ELE BATTERY REPORT ERROR! franchisee is not equal,franchiseeId1={},franchiseeId2={},sessionId={}", store.getFranchiseeId(), electricityBattery.getFranchiseeId(), sessionId);
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

    /**
     * 检查电池电压电流、充电器电压电流是否变化
     *
     * @param eleBox
     * @param electricityBattery
     * @param eleBatteryVO
     * @param sessionId
     */
    private void checkBatteryAndCharger(ElectricityCabinet electricityCabinet, ElectricityCabinetBox eleBox, ElectricityBattery electricityBattery, EleBatteryVO eleBatteryVO, String sessionId) {
        voltageCurrentExecutorService.execute(() -> {
            BatteryOtherPropertiesQuery batteryOtherPropertiesQuery = eleBatteryVO.getBatteryOtherProperties();
            if (Objects.isNull(batteryOtherPropertiesQuery)) {
                log.error("ELE BATTERY REPORT ERROR! batteryOtherPropertiesQuery is null,sessionId={}", sessionId);
                return;
            }

            BatteryOtherProperties batteryOtherProperties = batteryOtherPropertiesService.selectByBatteryName(electricityBattery.getSn());
            if (Objects.isNull(batteryOtherProperties)) {
                log.error("ELE BATTERY REPORT ERROR! batteryOtherProperties is null,sessionId={},sn={}", sessionId, electricityBattery.getSn());
                return;
            }

            boolean flag = !Objects.equals(eleBox.getChargeA(), eleBatteryVO.getChargeA())
                    || !Objects.equals(eleBox.getChargeV(), eleBatteryVO.getChargeV())
                    || !Objects.equals(batteryOtherProperties.getBatteryChargeA(), batteryOtherPropertiesQuery.getBatteryChargeA())
                    || !Objects.equals(batteryOtherProperties.getBatteryV(), batteryOtherPropertiesQuery.getBatteryV());

            if (flag) {
                saveVoltageCurrentToClickHouse(electricityCabinet, eleBatteryVO, sessionId);
            }
        });
    }

    /**
     * 电池电压电流、充电器电压电流保存到ClickHouse
     *
     * @param eleBatteryVO
     * @param sessionId
     */
    private void saveVoltageCurrentToClickHouse(ElectricityCabinet electricityCabinet, EleBatteryVO eleBatteryVO, String sessionId) {

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleBatteryVO.getReportTime()) ? 0L : eleBatteryVO.getReportTime());

        String sql = "insert into t_voltage_current_change (electricityCabinetId,cellNo,chargeV,chargeA,batteryChargeV,batteryChargeA,sessionId,reportTime,createTime) values(?,?,?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), eleBatteryVO.getCellNo(), eleBatteryVO.getChargeV(), eleBatteryVO.getChargeA(), eleBatteryVO.batteryOtherProperties.getBatteryV(),
                    eleBatteryVO.batteryOtherProperties.getBatteryChargeA(), sessionId, formatter.format(reportDateTime), formatter.format(LocalDateTime.now()));
        } catch (Exception e) {
            log.error("ELE BATTERY REPORT ERROR! save voltageCurrent to clickHouse error!", e);
        }
    }

    /**
     * 柜子电池满仓发送MQ通知
     *
     * @param electricityCabinet
     */
    private void checkElectricityCabinetBatteryFull(ElectricityCabinet electricityCabinet) {
    
        //获取所有启用的格挡
        List<ElectricityCabinetBox> electricityCabinetBoxes = electricityCabinetBoxService.queryBoxByElectricityCabinetId(electricityCabinet.getId());
        if (CollectionUtils.isEmpty(electricityCabinetBoxes)) {
            return;
        }
    
        //过滤没有电池的格挡
        List<ElectricityCabinetBox> haveBatteryBoxs = electricityCabinetBoxes.stream().filter(item -> StringUtils.isBlank(item.getSn())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(haveBatteryBoxs)) {
            return;
        }
    
        //柜机仓内电池已满  发送MQ消息通知
        Boolean cacheFlag = redisService.setNx(CacheConstant.FULL_BOX_ELECTRICITY_CACHE + electricityCabinet.getId(), "1", 3600 * 1000L, false);
        if (cacheFlag) {
            List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> messageNotifyList = buildAbnormalMessageNotify(electricityCabinet);
            if (CollectionUtils.isEmpty(messageNotifyList)) {
                return;
            }
        
            messageNotifyList.forEach(item -> {
                rocketMqService.sendAsyncMsg(MqConstant.TOPIC_MAINTENANCE_NOTIFY, JsonUtil.toJson(item), "", "", 0);
                log.info("ELE BATTERY REPORT INFO! ele abnormal notify,msg={}", JsonUtil.toJson(item));
            });
        }
    }
    
    private List<MqNotifyCommon<ElectricityAbnormalMessageNotify>> buildAbnormalMessageNotify(ElectricityCabinet electricityCabinet) {
        MaintenanceUserNotifyConfig notifyConfig = maintenanceUserNotifyConfigService.queryByTenantIdFromCache(electricityCabinet.getTenantId());
        if (Objects.isNull(notifyConfig) || StringUtils.isBlank(notifyConfig.getPhones())) {
            log.error("ELE BATTERY REPORT ERROR! not found maintenanceUserNotifyConfig,tenantId={}",
                    electricityCabinet.getTenantId());
            return Collections.EMPTY_LIST;
        }
        
        List<String> phones = JSON.parseObject(notifyConfig.getPhones(), List.class);
        if (CollectionUtils.isEmpty(phones)) {
            log.error("ELE BATTERY REPORT ERROR! phones is empty,tenantId={}", electricityCabinet.getTenantId());
            return Collections.EMPTY_LIST;
        }
        
        return phones.parallelStream().map(item -> {
            ElectricityAbnormalMessageNotify abnormalMessageNotify = new ElectricityAbnormalMessageNotify();
            abnormalMessageNotify.setAddress(electricityCabinet.getAddress());
            abnormalMessageNotify.setEquipmentNumber(electricityCabinet.getName());
            abnormalMessageNotify.setExceptionType(ElectricityAbnormalMessageNotify.BATTERY_FULL_TYPE);
            abnormalMessageNotify.setDescription(ElectricityAbnormalMessageNotify.BATTERY_FULL_MSG);
            abnormalMessageNotify.setReportTime(formatter.format(LocalDateTime.now()));
            
            MqNotifyCommon<ElectricityAbnormalMessageNotify> abnormalMessageNotifyCommon = new MqNotifyCommon<>();
            abnormalMessageNotifyCommon.setTime(System.currentTimeMillis());
            abnormalMessageNotifyCommon.setType(MqNotifyCommon.TYPE_ABNORMAL_ALARM);
            abnormalMessageNotifyCommon.setPhone(item);
            abnormalMessageNotifyCommon.setData(abnormalMessageNotify);
            return abnormalMessageNotifyCommon;
        }).collect(Collectors.toList());
        
    }

    /**
     * 解绑电池uid的同时  解绑用户绑定的电池
     *
     * @param batteryName
     */
//    private void unbindUserBattery(String batteryName) {
//        List<FranchiseeUserInfo> franchiseeUserInfoList = franchiseeUserInfoService
//            .selectByNowElectricityBatterySn(batteryName);
//        if (CollectionUtils.isEmpty(franchiseeUserInfoList)) {
//            return;
//        }
//
//        franchiseeUserInfoList.forEach(item -> {
//            FranchiseeUserInfo updateFranchiseeUserInfo = FranchiseeUserInfo.builder()
//                .id(item.getId())
//                .nowElectricityBatterySn(null)
//                .serviceStatus(item.getServiceStatus())
//                .updateTime(System.currentTimeMillis()).build();
//            franchiseeUserInfoService.unBind(updateFranchiseeUserInfo);
//        });
//    }
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




