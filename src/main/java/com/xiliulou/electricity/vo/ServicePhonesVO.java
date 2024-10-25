package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/24 16:25:01
 */
@Builder
@Data
public class ServicePhonesVO {
    
    private Integer tenantId;
    
    List<ServicePhoneVO> phoneList;
}
