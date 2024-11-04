package com.xiliulou.electricity.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @date 2024/10/31 07:56:19
 */
@Builder
@Data
public class ServicePhoneDTO {
    
    private String phone;
    
    private String remark;
}
