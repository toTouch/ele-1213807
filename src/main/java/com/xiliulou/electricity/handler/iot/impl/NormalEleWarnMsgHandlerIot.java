package com.xiliulou.electricity.handler.iot.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.TimeUtils;
import com.xiliulou.electricity.config.EleCommonConfig;
import com.xiliulou.electricity.config.TenantConfig;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author: hrp
 * @Date: 2022/09/20 17:02
 * @Description:
 */
@Service(value = ElectricityIotConstant.NORMAL_ELE_WARN_MSG_HANDLER)
@Slf4j
public class NormalEleWarnMsgHandlerIot extends AbstractElectricityIotHandler {

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
    @Autowired
    TenantConfig tenantConfig;


    public static final Integer CELL_ERROR_TYPE = 1;
    public static final Integer BATTERY_ERROR_TYPE = 2;
    public static final Integer CABINET_ERROR_TYPE = 3;
    public static final Integer BUSINESS_ERROR_TYPE = 4;


    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {

        String sessionId = receiverMessage.getSessionId();
        if (StrUtil.isEmpty(sessionId)) {
            log.error("ELE ERROR! warnMsgReport NO sessionId,{}", receiverMessage.getSessionId());
            return;
        }

        EleWarnMsgVo eleWarnMsgVo = JsonUtil.fromJson(receiverMessage.getOriginContent(), EleWarnMsgVo.class);
        if (Objects.isNull(eleWarnMsgVo)) {
            log.error("ELE ERROR! warnMsgReport is null,productKey={},sessionId,{}", receiverMessage.getProductKey(),receiverMessage.getSessionId());
            return;
        }

        if (Objects.equals(eleWarnMsgVo.getErrorType(), CELL_ERROR_TYPE)) {
            saveCellWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        } else if (Objects.equals(eleWarnMsgVo.getErrorType(), BATTERY_ERROR_TYPE)) {
            saveBatteryWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        } else if (Objects.equals(eleWarnMsgVo.getErrorType(), CABINET_ERROR_TYPE)) {
            saveCabinetWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        } else if (Objects.equals(eleWarnMsgVo.getErrorType(), BUSINESS_ERROR_TYPE)) {
            saveBusinessWarnMsgDataToClickHouse(electricityCabinet, eleWarnMsgVo);
        }

    }


    /**
     * 电池故障保存到clickhouse
     *
     * @param
     */
    private void saveBatteryWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_warn_msg_battery (electricityCabinetId,errorCode,sessionId,batteryName,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getBatteryName(), eleWarnMsgVo.getErrorMsg(),
                    createTime, reportTime,electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert batteryWarn sql error!", e);
        }
    }

    /**
     * 仓门故障保存到clickhouse
     *
     * @param
     */
    private void saveCellWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_warn_msg_cell (electricityCabinetId,errorCode,sessionId,cellNo,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getErrorMsg(),
                    createTime, reportTime,electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert cellWarn sql error!", e);
        }
    }

    /**
     * 柜机故障保存到clickhouse
     *
     * @param
     */
    private void saveCabinetWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_warn_msg_cabinet (electricityCabinetId,errorCode,sessionId,operateType,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getOperateType(), eleWarnMsgVo.getErrorMsg(),
                    createTime, reportTime,electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert cabinetWarn sql error!", e);
        }
    }

    /**
     * 业务故障保存到clickhouse
     *
     * @param
     */
    private void saveBusinessWarnMsgDataToClickHouse(ElectricityCabinet electricityCabinet, EleWarnMsgVo eleWarnMsgVo) {

        LocalDateTime now = LocalDateTime.now();
        String createTime = formatter.format(now);

        LocalDateTime reportDateTime = TimeUtils.convertLocalDateTime(Objects.isNull(eleWarnMsgVo.getCreateTime()) ? 0L : eleWarnMsgVo.getCreateTime());
        String reportTime = formatter.format(reportDateTime);

        String sql = "insert into t_warn_msg_business (electricityCabinetId,errorCode,sessionId,cellNo,operateType,errorMsg,createTime,reportTime,tenantId) values(?,?,?,?,?,?,?,?,?);";

        try {
            clickHouseService.insert(sql, electricityCabinet.getId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getSessionId(), eleWarnMsgVo.getCellNo(), eleWarnMsgVo.getOperateType(), eleWarnMsgVo.getErrorMsg(),
                    createTime, reportTime,electricityCabinet.getTenantId());
        } catch (Exception e) {
            log.error("ELE ERROR! clickHouse insert cabinetWarn sql error!", e);
        }
    }

    @Data
    class EleWarnMsgVo {

        private String sessionId;

        private Integer errorType;

        private Long createTime;

        private Long errorCode;

        private String batteryName;

        private String errorMsg;

        private Integer cellNo;

        private Integer operateType;
    }

}




