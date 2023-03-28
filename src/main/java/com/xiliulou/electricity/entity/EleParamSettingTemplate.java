package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (EleParamSettingTemplate)实体类
 *
 * @author Eclair
 * @since 2023-03-28 09:53:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_param_setting_template")
public class EleParamSettingTemplate {
    
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
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
