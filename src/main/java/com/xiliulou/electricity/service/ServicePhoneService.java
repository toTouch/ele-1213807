package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ServicePhone;
import com.xiliulou.electricity.request.ServicePhonesRequest;
import com.xiliulou.electricity.vo.ServicePhoneVO;
import com.xiliulou.electricity.vo.ServicePhonesVO;

import java.util.List;
import java.util.Objects;

/**
 * @author HeYafeng
 * @date 2024/10/24 17:40:22
 */
public interface ServicePhoneService {
    
    R insertOrUpdate(ServicePhonesRequest request);
    
    ServicePhonesVO queryByTenantIdFromCache(Integer tenantId);
    
    List<ServicePhone> listByIds(List<Long> ids);
    
    List<ServicePhoneVO> listByTenantId(Integer tenantId);
    
}
