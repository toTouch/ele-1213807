package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.ServicePhone;
import com.xiliulou.electricity.vo.ServicePhoneVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author HeYafeng
 * @date 2024/10/31 07:56:19
 */
@Builder
@Data
public class ServicePhoneDTO {
    
    private List<ServicePhone> insertList;
    
    private List<ServicePhone> updateList;
    
    private List<Long> deleteList;
    
    private Map<Long, ServicePhoneVO> existMap;
}
