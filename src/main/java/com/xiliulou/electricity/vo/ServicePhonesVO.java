package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:25:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ServicePhonesVO {
    
    private Integer tenantId;
    
    List<ServicePhoneVO> phoneList;
}
