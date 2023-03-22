package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zgw
 * @date 2023/3/22 18:29
 * @mood
 */
@Data
public class UserChannelQuery {
    
    @NotEmpty(message = "用户手机号不能为空", groups = {CreateGroup.class})
    private String phone;
    
    @NotEmpty(message = "用户名不能为空", groups = {CreateGroup.class})
    private String name;
}
