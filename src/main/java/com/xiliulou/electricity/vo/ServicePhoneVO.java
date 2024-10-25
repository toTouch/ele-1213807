package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:25:01
 */
@Builder
@Data
public class ServicePhoneVO {
    
    private Long id;
    
    private String phone;
    
    private String remark;
}
