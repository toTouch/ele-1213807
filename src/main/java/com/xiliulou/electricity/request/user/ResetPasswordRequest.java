package com.xiliulou.electricity.request.user;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:23:46
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ResetPasswordRequest {
    
    @NotNull(groups = UpdateGroup.class, message = "tenantId不能为空")
    private Integer tenantId;
    
    @NotNull(groups = UpdateGroup.class, message = "uid不能为空")
    private Long uid;
    
    @NotBlank(groups = UpdateGroup.class, message = "密码不能为空")
    private String password;
}
