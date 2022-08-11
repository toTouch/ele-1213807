package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.BigEleBatteryVo;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shaded.org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author: lxc
 * @Date: 2020/12/28 17:02
 * @Description:
 */
@Service(value= ElectricityIotConstant.NORMAL_ELE_BATTERY_HANDLER)
@Slf4j
public class NormalEleBatteryHandlerIot extends AbstractElectricityIotHandler {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    ClickHouseService clickHouseService;

    @Autowired
    EleCommonConfig eleCommonConfig;

    public static final String TERNARY_LITHIUM = "TERNARY_LITHIUM";
    public static final String IRON_LITHIUM = "IRON_LITHIUM";
    public static final String UNKNOW = "UNKNOW";


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        if (ElectricityIotConstant.BATTERY_CHANGE_REPORT.equals(receiverMessage.getType())) {
            EleBatteryChangeReportVO batteryChangeReportVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryChangeReportVO.class);
            if (Objects.isNull(batteryChangeReportVO)) {
                log.error("ELE ERROR! batteryChangeReport is null,productKey={}", receiverMessage.getProductKey());
                return;
            }

            //电池检测上报数据保存到ClickHouse
            saveReportDataToClickHouse( electricityCabinet,  receiverMessage,batteryChangeReportVO);

        } else {
            updateBatteryInfo(electricityCabinet, receiverMessage);
        }
    }


    private void updateBatteryInfo(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage){
        EleBatteryVo eleBatteryVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryVo.class);
        if (Objects.isNull(eleBatteryVo)) {
            log.error("ele battery error! no eleCellVo,{}", receiverMessage.getOriginContent());
            return ;
        }

        String batteryName = eleBatteryVo.getBatteryName();
        String cellNo = eleBatteryVo.getCellNo();
        Boolean existsBattery = eleBatteryVo.getExistsBattery();
        Long reportTime = eleBatteryVo.getReportTime();

        if (StringUtils.isEmpty(cellNo)) {
            log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
            return ;
        }

        ElectricityCabinetBox electricityCabinetBoxByDb = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
        if (Objects.isNull(electricityCabinetBoxByDb)) {
            log.error("ELE ERROR! no cellNo! p={},d={},cell={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName(), cellNo);
            return ;
        }

        //若上报时间小于上次上报时间则忽略此条上报
        if (Objects.nonNull(reportTime) && Objects.nonNull(electricityCabinetBoxByDb.getReportTime())
                && electricityCabinetBoxByDb.getReportTime() >= reportTime) {
            log.error("ele battery error! reportTime is less ,reportTime:{}", reportTime);
            return ;
        }

        //存在电池但是电池名字没有上报
        if (Objects.nonNull(existsBattery) && StringUtils.isEmpty(batteryName) && existsBattery) {
            log.error("ELE ERROR! battery report illegal! existsBattery={},batteryName={}", existsBattery, batteryName);
            return ;
        }

        //不存在电池 但上报电池编号不为空 处理为空
        if (Objects.nonNull(existsBattery) && !existsBattery && StrUtil.isNotEmpty(batteryName)) {
            log.warn("ELE WARN! battery report illegal! battery name is exists! but existsBattery is false ! batteryName ={}", batteryName);
            batteryName = null;
        }

        //仓门未上报电池
        if (StringUtils.isEmpty(batteryName)) {
            //空仓处理
            this.emptyEleBoxProcess(eleBatteryVo, electricityCabinet, electricityCabinetBoxByDb);
            return ;
        }

        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);

        //检查电池编号是否存在 并记录到表
        boolean isNotExistSn = this.checkNotExistSn(eleBatteryVo, electricityCabinet, electricityBattery);
        if(isNotExistSn) {
            return;
        }

        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.error("ele battery error! tenantId is not equal,tenantId1:{},tenantId2:{}", electricityCabinet.getTenantId(), electricityBattery.getTenantId());
            return ;
        }

        //更新电池 保存上报的额外信息
        //当electricityBattery 为空时 ，checkNotExistSn() 会返回 true ，所以这里不用判空。
        updateElectricityBatteryAndSaveOtherAttr(electricityCabinet, electricityBattery, eleBatteryVo);

        Boolean isBatteryBindFranchisee = checkBatteryBindFranchisee(electricityCabinet, electricityBattery);
        if(Objects.isNull(isBatteryBindFranchisee)) {
            return;
        }

        ElectricityCabinetBox updateEleBox = new ElectricityCabinetBox();
        //updateEleBox.setBatteryType(null);
        updateEleBox.setChargeV(eleBatteryVo.getChargeV());
        updateEleBox.setChargeA(eleBatteryVo.getChargeA());
        updateEleBox.setBatteryType(getBatteryModel(batteryName, eleBatteryVo.getIsMultiBatteryModel()));
        updateEleBox.setSn(isBatteryBindFranchisee ? electricityBattery.getSn() : UNKNOW + electricityBattery.getSn());
        updateEleBox.setStatus(isBatteryBindFranchisee ? ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY : ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
        updateEleBox.setElectricityCabinetId(electricityCabinet.getId());
        updateEleBox.setCellNo(cellNo);
        updateEleBox.setUpdateTime(System.currentTimeMillis());

        if (Objects.nonNull(reportTime)) {
            updateEleBox.setReportTime(reportTime);
        }

        electricityCabinetBoxService.modifyByCellNo(updateEleBox);

    }

    private Boolean checkBatteryBindFranchisee(ElectricityCabinet electricityCabinet, ElectricityBattery electricityBattery) {
        //查电池所属加盟商
        FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryId(electricityBattery.getId());
        if (Objects.isNull(franchiseeBindElectricityBattery)) {
            log.error("ele battery error! battery not bind franchisee,electricityBatteryId:{}", electricityBattery.getId());
            return false;
        }

        // 查换电柜所属加盟商
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("ele battery error! not find store,storeId:{}", electricityCabinet.getStoreId());
            return null;
        }

        if (!Objects.equals(store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId().longValue())) {
            log.error("ele battery error! franchisee is not equal,franchiseeId1:{},franchiseeId2:{}", store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId());
            return false;
        }
        return true;
    }

    private void updateElectricityBatteryAndSaveOtherAttr(ElectricityCabinet electricityCabinet,  ElectricityBattery electricityBattery, EleBatteryVo eleBatteryVo){
        ElectricityBattery updateElectricityBattery = new ElectricityBattery();
        updateElectricityBattery.setId(electricityBattery.getId());
        updateElectricityBattery.setModel(getBatteryModel(eleBatteryVo.getBatteryName(), eleBatteryVo.getIsMultiBatteryModel()));
        updateElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
        updateElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
        updateElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
        updateElectricityBattery.setLastDepositCellNo(eleBatteryVo.getCellNo());
        updateElectricityBattery.setUid(null);
        updateElectricityBattery.setBorrowExpireTime(null);
        updateElectricityBattery.setUpdateTime(System.currentTimeMillis());
        updateElectricityBattery.setPower(getPower(electricityBattery.getPower(), eleBatteryVo.getPower()));

        if (StringUtils.isNotEmpty(eleBatteryVo.getHealth())) {
            updateElectricityBattery.setHealthStatus(Integer.valueOf(eleBatteryVo.getHealth()));
        }

        if (StringUtils.isNotEmpty(eleBatteryVo.getChargeStatus())) {
            updateElectricityBattery.setChargeStatus(Integer.valueOf(eleBatteryVo.getChargeStatus()));
        }
        electricityBatteryService.updateByOrder(updateElectricityBattery);

        //电池上报是否有其他信息
        if (Objects.isNull(eleBatteryVo.getHasOtherAttr()) || !eleBatteryVo.getHasOtherAttr()) {
            return;
        }

        BatteryOtherPropertiesQuery batteryOtherPropertiesQuery = eleBatteryVo.getBatteryOtherProperties();
        BatteryOtherProperties batteryOtherProperties = new BatteryOtherProperties();
        BeanUtils.copyProperties(batteryOtherPropertiesQuery, batteryOtherProperties);
        batteryOtherProperties.setBatteryName(eleBatteryVo.getBatteryName());
        batteryOtherProperties.setBatteryCoreVList(JsonUtil.toJson(batteryOtherPropertiesQuery.getBatteryCoreVList()));
        batteryOtherPropertiesService.insertOrUpdate(batteryOtherProperties);
    }

    private Double getPower(Double batteryPower, Double reportPower){
        //上报电量和上次电量相差百分之50以上(不包含百分之百) 电量不做修改
        //上报电量百分比转化
        reportPower = Objects.isNull(reportPower) ? null : reportPower * 100;

        if (Objects.nonNull(eleCommonConfig.getBatteryReportCheck())
                && Objects.equals(eleCommonConfig.getBatteryReportCheck(), EleCommonConfig.OPEN_BATTERY_REPORT_CHECK)
                && Objects.nonNull(batteryPower)
                && Objects.nonNull(reportPower)
                && Math.abs(batteryPower - reportPower) >= 50
                && Math.abs(batteryPower - reportPower) != 100) {
            reportPower = batteryPower;
        }

        return reportPower;
    }

    private String getBatteryModel(String batteryName, Boolean isMultiBatteryModel){
        if (Objects.isNull(isMultiBatteryModel) || !isMultiBatteryModel) {
            return null;
        }

        return parseBatteryNameAcquireBatteryModel(batteryName);
    }

    private boolean checkNotExistSn(EleBatteryVo eleBatteryVo, ElectricityCabinet electricityCabinet,  ElectricityBattery electricityBattery){
        String batteryName = eleBatteryVo.getBatteryName();
        Integer cellNo = Integer.valueOf(eleBatteryVo.getCellNo());

        NotExistSn notExistSn = notExistSnService.queryByOther(batteryName, electricityCabinet.getId(), cellNo);
        if(Objects.nonNull(electricityBattery) && Objects.nonNull(notExistSn)) {
            notExistSn.setDelFlag(NotExistSn.DEL_DEL);
            notExistSn.setUpdateTime(System.currentTimeMillis());
            notExistSnService.update(notExistSn);
            return false;
        }

        if(Objects.nonNull(electricityBattery)) {
            return false;
        }

        log.error("ele battery error! no electricityBattery,sn,{}", batteryName);

        //本柜机本格挡插入过 则跳过
        if (Objects.nonNull(notExistSn)) {
            return true;
        }

        //该电池是否在其他柜机存放过
        NotExistSn notExistSnOld = notExistSnService.queryByBatteryName(batteryName);
        if (Objects.isNull(notExistSnOld)) {
            NotExistSn insertNotExistSn = new NotExistSn();
            insertNotExistSn.setEId(electricityCabinet.getId());
            insertNotExistSn.setBatteryName(batteryName);
            insertNotExistSn.setCellNo(Integer.valueOf(cellNo));
            insertNotExistSn.setCreateTime(System.currentTimeMillis());
            insertNotExistSn.setUpdateTime(System.currentTimeMillis());
            insertNotExistSn.setTenantId(electricityCabinet.getTenantId());
            notExistSnService.insert(insertNotExistSn);
            return true;
        }

        NotExistSn updateNotExistSn = new NotExistSn();
        updateNotExistSn.setId(notExistSnOld.getId());
        notExistSnOld.setEId(electricityCabinet.getId());
        notExistSnOld.setCellNo(Integer.valueOf(cellNo));
        notExistSnOld.setUpdateTime(System.currentTimeMillis());
        notExistSnService.update(notExistSnOld);
        return true;
    }

    private void emptyEleBoxProcess(EleBatteryVo eleBatteryVo, ElectricityCabinet electricityCabinet, ElectricityCabinetBox electricityCabinetBoxByDb){
        //TODO 电池上报电量大幅变化，忽略此条上报
        ElectricityCabinetBox updateEleBox = new ElectricityCabinetBox();

        //updateEleBox.setBatteryType(null);
        //updateEleBox.setSn(null);
        //updateEleBox.setPower(null);

        if (Objects.nonNull(eleBatteryVo.getReportTime())) {
            updateEleBox.setReportTime(eleBatteryVo.getReportTime());
        }

        updateEleBox.setChargeV(eleBatteryVo.getChargeV());
        updateEleBox.setChargeA(eleBatteryVo.getChargeA());
        updateEleBox.setElectricityCabinetId(electricityCabinet.getId());
        updateEleBox.setCellNo(eleBatteryVo.getCellNo());
        updateEleBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
        updateEleBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxService.modifyByCellNo(updateEleBox);

        //仓门如果无电池
        if (StringUtils.isEmpty(electricityCabinetBoxByDb.getSn())) {
            return;
        }

        //电池如果有Unknown前缀，则删除前缀Unknown
        removePrefixUnknownToBatterySn(electricityCabinetBoxByDb);

        //电池不存在或电池为租借状态
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(electricityCabinetBoxByDb.getSn());
        if (Objects.isNull(oldElectricityBattery)
                || Objects.equals(oldElectricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS)) {
            return;
        }

        //如果电池不是租借状态，修改电池为异常取走
        ElectricityBattery updateElectricityBattery = new ElectricityBattery();
        updateElectricityBattery.setId(oldElectricityBattery.getId());
        updateElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
        updateElectricityBattery.setElectricityCabinetId(null);
        updateElectricityBattery.setElectricityCabinetName(null);
        updateElectricityBattery.setUid(null);
        updateElectricityBattery.setUpdateTime(System.currentTimeMillis());
        electricityBatteryService.updateByOrder(updateElectricityBattery);
    }


    private void removePrefixUnknownToBatterySn(ElectricityCabinetBox electricityCabinetBox) {
        if (electricityCabinetBox.getSn().contains(UNKNOW)) {
            electricityCabinetBox.setSn(electricityCabinetBox.getSn().substring(UNKNOW.length()));
        }
    }

    /**
     * 检测电池数据保存到clickhouse
     * @param batteryChangeReport
     */
    private void saveReportDataToClickHouse(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage, EleBatteryChangeReportVO batteryChangeReport) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(batteryChangeReport.getCreateTime()) ? 0L : batteryChangeReport.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_battery_change (electricityCabinetId,cellNo,sessionId,preBatteryName,changeBatteryName,reportTime,createTime) values(?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), batteryChangeReport.getCellNo(), receiverMessage.getSessionId(), batteryChangeReport.getPreBatteryName(), batteryChangeReport.getChangeBatteryName(),
                    reportTime, createTime);
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert sql error!", e);
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
    class EleBatteryVo {
        private String batteryName;
        //电量
        private Double power;
        //健康状态
        private String health;
        //充电状态
        private String chargeStatus;
        //cellNo
        private String cellNo;
        //reportTime
        private Long reportTime;
        //充电器电压
        private Double chargeV;
        //充电器电流
        private Double chargeA;

        private Boolean existsBattery;
        //是否混合模式
        private Boolean isMultiBatteryModel;

        private Boolean hasOtherAttr;

        private BatteryOtherPropertiesQuery batteryOtherProperties;

    }

    @Data
    class EleBatteryChangeReportVO {
        private Integer cellNo;
        private String sessionId;
        private String productKey;
        private String preBatteryName;
        private String changeBatteryName;
        private Long createTime;
    }

}




