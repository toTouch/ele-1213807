package com.xiliulou.electricity.request.userinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:23:46
 */

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserInfoLimitRequest {
    
    @NotNull(message = "uid不能为空")
    private Long uid;
    
    @NotNull(message = "eleLimit不能为空")
    private Integer eleLimit;
}