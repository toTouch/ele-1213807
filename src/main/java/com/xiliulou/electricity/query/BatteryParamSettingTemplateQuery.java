package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/3/29 13:37
 * @mood
 */
@Data
public class BatteryParamSettingTemplateQuery {
    
    @NotNull(message = "模板名称不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 模板名
     */
    @NotNull(message = "模板名称不能为空")
    private String name;
    
    @NotEmpty(message = "模板配置内容不能为空")
    private String config;
}
