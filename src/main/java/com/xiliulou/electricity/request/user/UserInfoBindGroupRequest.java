package com.xiliulou.electricity.request.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author HeYafeng
 * @description 会员详情-加入分组
 * @date 2024/4/8 19:45:12
 */

@Data
public class UserInfoBindGroupRequest {
    
    @NotNull(message = "uid不能为空")
    private Long uid;
    
    @NotEmpty(message = "groupIds不能为空")
    private List<Long> groupIds;
}
    
