package com.xiliulou.electricity.handler.iot.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.dto.BatteryMultiConfigDTO;
import com.xiliulou.electricity.dto.ElectricityCabinetOtherSetting;
import com.xiliulou.electricity.entity.BatteryChargeConfig;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.handler.iot.AbstractElectricityIotHandler;
import com.xiliulou.electricity.query.BatteryChargeConfigQuery;
import com.xiliulou.electricity.service.BatteryChargeConfigService;
import com.xiliulou.iot.entity.ReceiverMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author zgw
 * @date 2023/3/31 11:00
 * @mood
 */
@Service(value = ElectricityIotConstant.NORMAL_OTHER_SETTING_PARAM_TEMPLATE_HANDLER)
@Slf4j
public class NormalOtherSettingParamTemplateHandler extends AbstractElectricityIotHandler {
    
    @Autowired
    RedisService redisService;
    
    @Autowired
    BatteryChargeConfigService batteryChargeConfigService;
    
    @Override
    public void postHandleReceiveMsg(ElectricityCabinet electricityCabinet, ReceiverMessage receiverMessage) {
        
        OtherSettingParamTemplateRequest otherSetting = JsonUtil
                .fromJson(receiverMessage.getOriginContent(), OtherSettingParamTemplateRequest.class);
        if (Objects.isNull(otherSetting)) {
            log.error("OTHER CONFIG ERROR! sessionId={}", receiverMessage.getSessionId());
            return;
        }
        
        BatteryChargeConfigQuery batteryChargeConfigQuery = new BatteryChargeConfigQuery();
        batteryChargeConfigQuery.setApplicationModel(otherSetting.getApplicationMode());
        batteryChargeConfigQuery.setConfigList(otherSetting.getBatteryMultiConfigList());
        batteryChargeConfigQuery.setElectricityCabinetId(electricityCabinet.getId().longValue());
        batteryChargeConfigQuery.setDelFlag(BatteryChargeConfig.DEL_NORMAL);
        batteryChargeConfigQuery.setTenantId(electricityCabinet.getTenantId());
        batteryChargeConfigQuery.setCreateTime(System.currentTimeMillis());
        batteryChargeConfigQuery.setUpdateTime(System.currentTimeMillis());
        batteryChargeConfigService.insertOrUpdate(batteryChargeConfigQuery);
        
        ElectricityCabinetOtherSetting electricityCabinetOtherSetting = new ElectricityCabinetOtherSetting();
        BeanUtils.copyProperties(otherSetting, electricityCabinetOtherSetting);
        
        redisService.saveWithHash(CacheConstant.OTHER_CONFIG_CACHE_V_2 + electricityCabinet.getId(),
                electricityCabinetOtherSetting);
        redisService.saveWithString(CacheConstant.ELE_OPERATOR_CACHE_KEY + receiverMessage.getSessionId(), "ok", 30L,
                TimeUnit.SECONDS);
    }
    
    @Data
    class OtherSettingParamTemplateRequest {
        
        /**
         * 系统导航栏
         */
        private Integer systemBarStatus;
        
        /**
         * 反锁
         */
        private Integer revertLockStatus;
        
        /**
         * iot心跳检测
         */
        private Integer iotCheckStatus;
        
        /**
         * 自动温控
         */
        private Integer autoTempControlStatus;
        
        /**
         * 新硬件状态
         */
        private Integer newHardwareStatus;
        
        /**
         * 底层日志开启
         */
        private Integer logStatus;
        
        /**
         * 应用模式
         */
        private String applicationMode;
        
        /**
         * 加热的最低阀值
         */
        private String openHeatCondition;
        
        /**
         * 散热的起始条件
         */
        private String openFanCondition;
        
        /**
         * 普通模式下的充电电压
         */
        private String normalChargeV;
        
        /**
         * 普通模式下的充电电流
         */
        private String normalChargeA;
        
        /**
         * 换电标准
         */
        private String exchangeCondition;
        
        /**
         * 同时充电的个数
         */
        private Integer chargingNum;
        
        /**
         * 轮训上报的时间
         */
        private Integer loopTimeReport;
        
        /**
         * 充电停止上限
         */
        private String chargeMaxCondition;
        
        /**
         * 充电停止上限(电压)
         */
        private String chargeMaxConditionV;
        
        /**
         * 充电停止下限
         */
        private String chargeMinCondition;
        
        /**
         * 充电停止下限(电压)
         */
        private String chargeMinConditionV;
        
        /**
         * api地址
         */
        private String apiAddress;
        
        /**
         * 二维码地址
         */
        private String qrAddress;
        
        /**
         * 三元组下载地址
         */
        private String downloadAddress;
        
        /**
         * 耗电量倍率
         */
        private Integer powerConsumptionMultiply;
        
        /**
         * 显示真实电量
         * <p>
         * multi_v模式
         */
        private Integer realQuantity;
        
        /**
         * 满电判断标准 multi——v模式
         */
        private Integer maxBatteryStandard;
        
        private Integer enableBatteryExceptionCheck;
        
        private String bms;
        
        private Integer serverHeartBeat;
        
        /**
         * 开启电池BMS异常检测
         */
        private Integer enableBatteryBMSExceptionCheck;
        
        private List<BatteryMultiConfigDTO> batteryMultiConfigList;
    
        /**
         * 充电器保护
         */
        private Integer defaultChargeStorageProtect;
        
        /**
         * 检测满电电流（1 关闭 0开启）
         */
        private Integer isCheckChargeA;
        
        /**
         * 反向供电检测（1 关闭 0开启）
         */
        private Integer isBackupPower;
        
        /**
         * 电池激活电压
         */
        private Float chargeActivationV;
        
        /**
         * 电池激活电流
         */
        private Integer chargeActivationA;
    
        /**
         * 气溶胶检测(1 关闭 0 开启)
         */
        private Integer checkFireWarning;
    
        /**
         * 水消防检测(1 关闭 0 开启)
         */
        private Integer checkWaterFireWarning;
    
        /**
         * 烟感检测（1 关闭 0 开启）
         */
        private Integer checkSmokeSensor;
        
        /**
         * 是否开启分时段充电
         */
        private Integer checkTimedCharge;
        
        /**
         * 分时段充电参数
         */
        private String timedChargeParams;
        
        /**
         * 柜机断电锁仓
         * <pre>
         *     0-开
         *     1-关
         * </pre>
         */
        private Integer restartLockCell;
        
        /**
         * 智能充电
         * <pre>
         *     0 - 打开
         *     1 - 关闭
         * </pre>
         */
        private Integer enableBmsChargeParams;
        
        /**
         * 在位检测 0：关闭 1：打开  （默认关闭）
         */
        private Integer checkBatteryExit;
    
        /**
         * 充电方式
         */
        private Integer whilstCharging;
    
        /**
         * 总功率
         */
        private Integer sumPowerLimit;
    
        /**
         * 整柜加热功率
         */
        private Integer cabinetHeatPower;
    
        /**
         * 单个充电器最大功率
         */
        private Integer cellChargingMaxPower;
    }
}
