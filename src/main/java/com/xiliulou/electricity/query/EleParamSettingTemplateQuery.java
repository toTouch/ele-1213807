package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/3/28 16:06
 * @mood
 */
@Data
public class EleParamSettingTemplateQuery {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 模板名
     */
    private String name;
    
    /**
     * 应用类型
     */
    private String applicationMode;
    
    /**
     * 电池充电停止下限
     */
    private Integer chargeMinCondition;
    
    /**
     * 电池充电停止上限
     */
    private Integer chargeMaxCondition;
    
    /**
     * 电池充电停止上限(电压)
     */
    private Integer chargeMaxConditionV;
    
    /**
     * 电池充电停止下限(电压)
     */
    private Integer chargeMinConditionV;
    
    /**
     * 充电数量
     */
    private Integer chargingNum;
    
    /**
     * 非标充电电流
     */
    private Integer normalChargeA;
    
    /**
     * 非标充电电压
     */
    private Integer normalChargeV;
    
    /**
     * 低温加热
     */
    private Integer openHeatCondition;
    
    /**
     * 高温散热
     */
    private Integer openFanCondition;
    
    /**
     * 耗电量倍率
     */
    private Integer powerConsumptionMultiply;
    
    /**
     * 系统按键  0关 1开
     */
    private Integer systemBarStatus;
    
    /**
     * 反锁(默认即可) 1关 0开
     */
    private Integer revertLockStatus;
    
    /**
     * 自动温控 1关 0开
     */
    private Integer autoTempControlStatus;
    
    /**
     * 底层日志 1关 0开
     */
    private Integer logStatus;
    
    /**
     * 电池异常检测  1关 0开
     */
    private Integer enableBatteryExceptionCheck;
    
    /**
     * 服务器心跳检测  1关 0开
     */
    private Integer serverHeartBeat;
    
    /**
     * enableBatteryBMSExceptionCheck 电池健康状态检测  1关 0开
     */
    private Integer enableBatteryBMSExceptionCheck;
}
