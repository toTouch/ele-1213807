package com.xiliulou.electricity.request;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:23:46
 */
@Data
public class ServicePhoneRequest {
    
    private Long id;
    
    @Size(max = 20, message = "电话格式错误，请检查！")
    private String phone;
    
    @Size(max = 10, message = "文案长度超限，最长为10位，请检查！")
    private String remark;
}
