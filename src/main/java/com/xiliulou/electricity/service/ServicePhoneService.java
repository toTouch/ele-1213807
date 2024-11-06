package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ServicePhone;
import com.xiliulou.electricity.request.ServicePhoneRequest;
import com.xiliulou.electricity.vo.ServicePhoneVO;

import java.util.List;

/**
 * @author HeYafeng
 * @date 2024/10/24 17:40:22
 */
public interface ServicePhoneService {
    
    R insertOrUpdate(List<ServicePhoneRequest> requestPhoneList);
    
    ServicePhone queryByTenantIdFromCache(Integer tenantId);
    
    List<ServicePhoneVO> listPhones(Integer tenantId);
}
