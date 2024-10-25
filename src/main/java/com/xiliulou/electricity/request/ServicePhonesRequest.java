package com.xiliulou.electricity.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:15:39
 */
@Data

public class ServicePhonesRequest {
    
    @Size(min = 1, max = 5, message = "最多只能添加5个手机号")
    @NotEmpty(message = "手机号不能为空")
    private List<ServicePhoneRequest> phoneList;
}


