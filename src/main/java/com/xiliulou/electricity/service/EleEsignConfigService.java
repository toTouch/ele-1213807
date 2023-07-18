package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleEsignConfig;
import com.xiliulou.electricity.query.EleEsignConfigQuery;

/**
 * @author: Kenneth
 * @Date: 2023/7/7 23:14
 * @Description:
 */
public interface EleEsignConfigService {

    EleEsignConfig selectLatestByTenantId(Integer tenantId);

    Integer insertOrUpdate(EleEsignConfigQuery eleEsignConfigQuery);

}
