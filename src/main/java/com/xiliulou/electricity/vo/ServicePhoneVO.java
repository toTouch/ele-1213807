package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:25:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ServicePhoneVO {
    
    private Long id;
    
    private String phone;
    
    private String remark;
}
