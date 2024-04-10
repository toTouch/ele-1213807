package com.xiliulou.electricity.request.user;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author HeYafeng
 * @description 导入用户
 * @date 2024/4/9 11:44:36
 */
@Data
public class UserInfoGroupBatchImportRequest {
    
    @NotNull(message = "加盟商ID不能为空")
    private Long franchiseeId;
    
    @Size(max = 10, message = "分组名称不能超过10个")
    @NotEmpty(message = "分组名称不能为空")
    private List<Long> groupIds;
    
    @NotNull(message = "用户电话号不能为空")
    private String jsonPhones;
}
