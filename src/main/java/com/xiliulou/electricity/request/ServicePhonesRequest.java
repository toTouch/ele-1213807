package com.xiliulou.electricity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:15:39
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ServicePhonesRequest {
    
    @NotEmpty(message = "手机号不能为空")
    private List<ServicePhoneRequest> phoneList;
}


