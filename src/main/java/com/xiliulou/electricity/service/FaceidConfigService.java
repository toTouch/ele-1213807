package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FaceidConfig;
import com.xiliulou.electricity.query.FaceidConfigQuery;

/**
 * (FaceidConfig)表服务接口
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
public interface FaceidConfigService {

    FaceidConfig selectLatestByTenantId(Integer tenantId);


    Integer insertOrUpdate(FaceidConfigQuery faceidConfigQuery);
}
