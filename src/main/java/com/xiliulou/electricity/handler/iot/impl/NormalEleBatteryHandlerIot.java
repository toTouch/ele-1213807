package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
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

    public static final String TERNARY_LITHIUM = "TERNARY_LITHIUM";
    public static final String IRON_LITHIUM = "IRON_LITHIUM";


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

        String cellNo = eleBatteryVo.getCellNo();
        if (StringUtils.isEmpty(cellNo)) {
            log.error("ele cell error! no eleCellVo,{}", receiverMessage.getOriginContent());
            return ;
        }

        ElectricityCabinetBox oldElectricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinet.getId(), cellNo);
        if (Objects.isNull(oldElectricityCabinetBox)) {
            log.error("ELE ERROR! no cellNo! p={},d={},cell={}", receiverMessage.getProductKey(), receiverMessage.getDeviceName(), cellNo);
            return ;
        }

        ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
        ElectricityBattery newElectricityBattery = new ElectricityBattery();

        electricityCabinetBox.setBatteryType(null);
        electricityCabinetBox.setChargeV(eleBatteryVo.getChargeV());
        electricityCabinetBox.setChargeA(eleBatteryVo.getChargeA());

        //若上报时间小于上次上报时间则忽略此条上报
        Long reportTime = eleBatteryVo.getReportTime();
        if (Objects.nonNull(reportTime) && Objects.nonNull(oldElectricityCabinetBox.getReportTime())
                && oldElectricityCabinetBox.getReportTime() >= reportTime) {
            log.error("ele battery error! reportTime is less ,reportTime:{}", reportTime);
            return ;
        }

        if (Objects.nonNull(reportTime)) {
            electricityCabinetBox.setReportTime(reportTime);
        }

        String batteryName = eleBatteryVo.getBatteryName();
        Boolean existsBattery = eleBatteryVo.getExistsBattery();

        //存在电池但是电池名字没有上报
        if (Objects.nonNull(existsBattery) && StringUtils.isEmpty(batteryName) && existsBattery) {
            log.error("ELE ERROR! battery report illegal! existsBattery={},batteryName={}", existsBattery, batteryName);
            return ;
        }

        //缓存存换电柜中电量最多的电池
        BigEleBatteryVo bigEleBatteryVo = redisService.getWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY + electricityCabinet.getId().toString(), BigEleBatteryVo.class);

        //不存在电池
        if (Objects.nonNull(existsBattery) && !existsBattery && StrUtil.isNotEmpty(batteryName)) {
            log.warn("ELE WARN! battery report illegal! battery name is exists! but existsBattery is false ! batteryName ={}", batteryName);
            batteryName = null;
        }

        if (StringUtils.isEmpty(batteryName)) {


            //TODO 电池上报电量大幅变化，忽略此条上报

            electricityCabinetBox.setSn(null);
            electricityCabinetBox.setPower(null);
            electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
            electricityCabinetBox.setCellNo(cellNo);
            electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
            electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
            electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);

            //原来仓门是否有电池
            if (StringUtils.isNotEmpty(oldElectricityCabinetBox.getSn())) {

                if (oldElectricityCabinetBox.getSn().contains("UNKNOW")) {
                    oldElectricityCabinetBox.setSn(oldElectricityCabinetBox.getSn().substring(6));
                }


                //修改电池
                ElectricityBattery oldElectricityBattery = electricityBatteryService.queryBySn(oldElectricityCabinetBox.getSn());
                if (Objects.nonNull(oldElectricityBattery) && !Objects.equals(oldElectricityBattery.getStatus(), ElectricityBattery.LEASE_STATUS)) {
                    newElectricityBattery.setId(oldElectricityBattery.getId());
                    newElectricityBattery.setStatus(ElectricityBattery.EXCEPTION_STATUS);
                    newElectricityBattery.setElectricityCabinetId(null);
                    newElectricityBattery.setElectricityCabinetName(null);
                    newElectricityBattery.setUid(null);
                    newElectricityBattery.setUpdateTime(System.currentTimeMillis());
                    electricityBatteryService.updateByOrder(newElectricityBattery);
                }
            }

            //若最大电池的仓门现在为空，则删除最大电量的缓存
            if (Objects.nonNull(bigEleBatteryVo) && Objects.equals(bigEleBatteryVo.getCellNo(), cellNo)) {
                redisService.delete(electricityCabinet.getId().toString());
            }
            return ;
        }

        NotExistSn oldNotExistSn = notExistSnService.queryByOther(batteryName, electricityCabinet.getId(), Integer.valueOf(cellNo));

        ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(batteryName);
        if (Objects.isNull(electricityBattery)) {
            log.error("ele battery error! no electricityBattery,sn,{}", batteryName);


            //插入表
            if (Objects.isNull(oldNotExistSn)) {
                NotExistSn notExistSnOld = notExistSnService.queryByBatteryName(batteryName);
                if (Objects.isNull(notExistSnOld)) {
                    NotExistSn notExistSn = new NotExistSn();
                    notExistSn.setEId(electricityCabinet.getId());
                    notExistSn.setBatteryName(batteryName);
                    notExistSn.setCellNo(Integer.valueOf(cellNo));
                    notExistSn.setCreateTime(System.currentTimeMillis());
                    notExistSn.setUpdateTime(System.currentTimeMillis());
                    notExistSn.setTenantId(electricityCabinet.getTenantId());
                    notExistSnService.insert(notExistSn);
                } else {
                    notExistSnOld.setEId(electricityCabinet.getId());
                    notExistSnOld.setCellNo(Integer.valueOf(cellNo));
                    notExistSnOld.setUpdateTime(System.currentTimeMillis());
                    notExistSnService.update(notExistSnOld);
                }
            }
            return ;
        }

        //查询表中是否有电池
        if (Objects.nonNull(oldNotExistSn)) {
            oldNotExistSn.setDelFlag(NotExistSn.DEL_DEL);
            oldNotExistSn.setUpdateTime(System.currentTimeMillis());
            notExistSnService.update(oldNotExistSn);
        }

        if (!Objects.equals(electricityCabinet.getTenantId(), electricityBattery.getTenantId())) {
            log.error("ele battery error! tenantId is not equal,tenantId1:{},tenantId2:{}", electricityCabinet.getTenantId(), electricityBattery.getTenantId());
            return ;
        }

        //根据电池查询仓门类型
        if (Objects.nonNull(eleBatteryVo.getIsMultiBatteryModel()) && eleBatteryVo.getIsMultiBatteryModel()) {
            String batteryModel = parseBatteryNameAcquireBatteryModel(batteryName);
            electricityCabinetBox.setBatteryType(batteryModel);
            newElectricityBattery.setModel(batteryModel);
        }

        //修改电池
        newElectricityBattery.setId(electricityBattery.getId());
        newElectricityBattery.setStatus(ElectricityBattery.WARE_HOUSE_STATUS);
        newElectricityBattery.setElectricityCabinetId(electricityCabinet.getId());
        newElectricityBattery.setElectricityCabinetName(electricityCabinet.getName());
        newElectricityBattery.setLastDepositCellNo(cellNo);
        newElectricityBattery.setUid(null);
        newElectricityBattery.setBorrowExpireTime(null);
        newElectricityBattery.setUpdateTime(System.currentTimeMillis());
        //newElectricityBattery.setReportType(ElectricityBattery.REPORT_TYPE_ELECTRICITY_CABINET);
        Double power = eleBatteryVo.getPower();
        //上报电量和上次电量相差百分之50以上，电量不做修改
//        if (Objects.nonNull(electricityBattery.getPower()) && Objects.nonNull(power) && (electricityBattery.getPower() - (power * 100)) >= 50) {
//            power = (electricityBattery.getPower()) / 100;
//        }

        if (Objects.nonNull(power)) {
            newElectricityBattery.setPower(power * 100);
        }


        String health = eleBatteryVo.getHealth();
        if (StringUtils.isNotEmpty(health)) {
            newElectricityBattery.setHealthStatus(Integer.valueOf(health));
        }
        String chargeStatus = eleBatteryVo.getChargeStatus();
        if (StringUtils.isNotEmpty(chargeStatus)) {
            newElectricityBattery.setChargeStatus(Integer.valueOf(chargeStatus));
        }
        electricityBatteryService.updateByOrder(newElectricityBattery);

        //电池上报是否有其他信息
        if (Objects.nonNull(eleBatteryVo.getHasOtherAttr()) && eleBatteryVo.getHasOtherAttr()) {
            BatteryOtherPropertiesQuery batteryOtherPropertiesQuery = eleBatteryVo.getBatteryOtherProperties();
            BatteryOtherProperties batteryOtherProperties = new BatteryOtherProperties();
            BeanUtils.copyProperties(batteryOtherPropertiesQuery, batteryOtherProperties);
            batteryOtherProperties.setBatteryName(batteryName);
            batteryOtherProperties.setBatteryCoreVList(JsonUtil.toJson(batteryOtherPropertiesQuery.getBatteryCoreVList()));
            batteryOtherPropertiesService.insertOrUpdate(batteryOtherProperties);
        }

        //比较最大电量，保证仓门电池是最大电量的电池
        if (Objects.isNull(eleBatteryVo.getIsMultiBatteryModel()) || !eleBatteryVo.getIsMultiBatteryModel()) {
            Double nowPower = eleBatteryVo.getPower();
            BigEleBatteryVo newBigEleBatteryVo = new BigEleBatteryVo();
            newBigEleBatteryVo.setCellNo(cellNo);
            if (Objects.isNull(bigEleBatteryVo)) {
                newBigEleBatteryVo.setPower(nowPower);
                redisService.saveWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY + electricityCabinet.getId().toString(), newBigEleBatteryVo);
            } else {
                Double oldPower = bigEleBatteryVo.getPower();
                if (Objects.nonNull(oldPower) && Objects.nonNull(nowPower) && nowPower > oldPower) {
                    newBigEleBatteryVo.setPower(nowPower);
                    redisService.saveWithHash(ElectricityCabinetConstant.ELE_BIG_POWER_CELL_NO_CACHE_KEY + electricityCabinet.getId().toString(), newBigEleBatteryVo);
                }
            }
        }

        //修改仓门
        electricityCabinetBox.setSn(electricityBattery.getSn());
        electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY);

        //查电池所属加盟商
        FranchiseeBindElectricityBattery franchiseeBindElectricityBattery = franchiseeBindElectricityBatteryService.queryByBatteryId(electricityBattery.getId());
        if (Objects.isNull(franchiseeBindElectricityBattery)) {
            log.error("ele battery error! battery not bind franchisee,electricityBatteryId:{}", electricityBattery.getId());
            electricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
            electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
        } else {

            // 查换电柜所属加盟商
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("ele battery error! not find store,storeId:{}", electricityCabinet.getStoreId());
                return ;
            }

            if (!Objects.equals(store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId().longValue())) {
                log.error("ele battery error! franchisee is not equal,franchiseeId1:{},franchiseeId2:{}", store.getFranchiseeId(), franchiseeBindElectricityBattery.getFranchiseeId());
                electricityCabinetBox.setSn("UNKNOW" + electricityBattery.getSn());
                electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
            }
        }

        electricityCabinetBox.setElectricityCabinetId(electricityCabinet.getId());
        electricityCabinetBox.setCellNo(cellNo);
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxService.modifyByCellNo(electricityCabinetBox);

    }

    /**
     * 检测电池数据保存到clickhouse
     * @param batteryChangeReport
     */
    private void saveReportDataToClickHouse(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage,EleBatteryChangeReportVO batteryChangeReport) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(batteryChangeReport.getCreateTime()) ? 0L : batteryChangeReport.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_battery_change (electricityCabinetId,sessionId,preBatteryName,changeBatteryName,reportTime,createTime) values(?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(),receiverMessage.getSessionId(), batteryChangeReport.getPreBatteryName(), batteryChangeReport.getChangeBatteryName(),
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

        private Boolean isMultiBatteryModel;

        private Boolean hasOtherAttr;

        private BatteryOtherPropertiesQuery batteryOtherProperties;

    }

    @Data
    class EleBatteryChangeReportVO {
        private String sessionId;
        private String productKey;
        private String preBatteryName;
        private String changeBatteryName;
        private Long createTime;
    }

}




