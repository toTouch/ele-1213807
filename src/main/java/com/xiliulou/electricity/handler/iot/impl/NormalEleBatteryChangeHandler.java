package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 电池变化
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-09-10:47
 */
@Slf4j
@Service(value = ElectricityIotConstant.NORMAL_ELE_BATTERY_CHANGE_HANDLER)
public class NormalEleBatteryChangeHandler extends AbstractElectricityIotHandler {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    ClickHouseService clickHouseService;

    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        EleBatteryChangeReportVO batteryChangeReportVO = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleBatteryChangeReportVO.class);
        if (Objects.isNull(batteryChangeReportVO)) {
            log.error("ELE ERROR! batteryChangeReport is null,productKey={}", receiverMessage.getProductKey());
            return;
        }

        //电池检测上报数据保存到ClickHouse
        saveReportDataToClickHouse(electricityCabinet, receiverMessage, batteryChangeReportVO);
    }

    /**
     * 检测电池数据保存到clickhouse
     *
     * @param batteryChangeReport
     */
    private void saveReportDataToClickHouse(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage, EleBatteryChangeReportVO batteryChangeReport) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(batteryChangeReport.getCreateTime()) ? 0L : batteryChangeReport.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_battery_change (electricityCabinetId,cellNo,sessionId,preBatteryName,changeBatteryName,reportTime,createTime,operateType,orderId) values(?,?,?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), batteryChangeReport.getCellNo(), receiverMessage.getSessionId(), batteryChangeReport.getPreBatteryName(), batteryChangeReport.getChangeBatteryName(),
                    reportTime, createTime, batteryChangeReport.getOperateType(), batteryChangeReport.getOrderId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert sql error!", e);
        }
    }

    @Data
    class EleBatteryChangeReportVO {
        private Integer cellNo;
        private String sessionId;
        private String productKey;
        private String preBatteryName;
        private String changeBatteryName;
        private Long createTime;
        private Integer operateType;
        private String orderId;

//        public static final Integer TYPE_ORDER=1;
//        public static final Integer TYPE_EXCEPTION=2;
//        public static final Integer TYPE_OPERATE=3;
    }

}
