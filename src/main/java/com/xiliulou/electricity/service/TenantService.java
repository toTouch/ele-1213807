package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.query.TenantAddAndUpdateQuery;
import com.xiliulou.electricity.query.TenantQuery;

/**
 * 租户表(Tenant)表服务接口
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
public interface TenantService {

    /**
     * 新增数据
     *
     * @param tenantAddAndUpdateQuery 实例对象
     * @return 实例对象
     */
    R addTenant(TenantAddAndUpdateQuery tenantAddAndUpdateQuery);

    R editTenant(TenantAddAndUpdateQuery tenantAddAndUpdateQuery);

    R queryListTenant(TenantQuery tenantQuery);

    Tenant queryByIdFromCache(Integer tenantId);

	R queryCount(TenantQuery tenantQuery);

	Integer querySumCount(TenantQuery tenantQuery);


}
