package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (BatteryParamSettingTemplate)实体类
 *
 * @author Eclair
 * @since 2023-03-29 09:20:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_battery_param_setting_template")
public class BatteryParamSettingTemplate {
    
    private Long id;
    
    /**
     * 模板名
     */
    private String name;
    
    private String config;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
    
    private Integer delFlag;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
