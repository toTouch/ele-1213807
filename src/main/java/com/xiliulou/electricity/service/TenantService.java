package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.query.TenantQuery;

import java.util.List;

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
     * @param tenantQuery 实例对象
     * @return 实例对象
     */
    R addTenantId(TenantQuery tenantQuery);

    /**
     * 修改数据
     *
     * @param tenant 实例对象
     * @return 实例对象
     */
    Integer update(Tenant tenant);


}
