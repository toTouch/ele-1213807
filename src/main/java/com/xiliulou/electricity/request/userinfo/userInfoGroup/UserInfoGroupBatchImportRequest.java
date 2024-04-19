package com.xiliulou.electricity.request.userinfo.userInfoGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @description 导入用户
 * @date 2024/4/9 11:44:36
 */
@Data
public class UserInfoGroupBatchImportRequest {
    
    private Long franchiseeId;
    
    @NotNull(message = "分组ID不能为空")
    private Long groupId;
    
    @NotNull(message = "用户电话号不能为空")
    private String jsonPhones;
}
