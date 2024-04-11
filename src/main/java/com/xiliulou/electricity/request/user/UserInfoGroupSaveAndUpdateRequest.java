package com.xiliulou.electricity.request.user;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author HeYafeng
 * @description 新增用户分组
 * @date 2024/4/8 19:45:12
 */

@Data
public class UserInfoGroupSaveAndUpdateRequest {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    private Long franchiseeId;
    
    @Size(max = 30, message = "分组名称字数超出最大限制30字", groups = {CreateGroup.class, UpdateGroup.class})
    @NotNull(message = "分组名称不能为空")
    private String name;
}
