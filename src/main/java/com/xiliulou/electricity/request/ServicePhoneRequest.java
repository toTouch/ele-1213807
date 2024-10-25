package com.xiliulou.electricity.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:23:46
 */
@Builder
@Data
public class ServicePhoneRequest {
    
    private Long id;
    
    @NotNull(message = "手机号不能为空")
    private String phone;
    
    @Size(max = 10, message = "文案长度超限，最长为10位，请检查！")
    private String remark;
}
