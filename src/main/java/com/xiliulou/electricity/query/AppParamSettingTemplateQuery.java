package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author zgw
 * @date 2023/3/30 20:21
 * @mood
 */
@Data
public class AppParamSettingTemplateQuery {
    
    @NotNull(message = "id不能为空", groups = UpdateGroup.class)
    private Long id;
    
    /**
     * 模板名
     */
    @NotEmpty(message = "模板名不能为空", groups = CreateGroup.class)
    @Size(max = 10, message = "模板名长度超限，最长10位，请检查!", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    @NotEmpty(message = "模板参数不能为空")
    private String configContent;
}
