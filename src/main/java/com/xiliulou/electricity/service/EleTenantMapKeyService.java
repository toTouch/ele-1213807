package com.xiliulou.electricity.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleTenantMapKey;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.query.EleTenantMapKeyAddAndUpdate;
import com.xiliulou.electricity.query.ElectricityConfigAddAndUpdateQuery;


/**
 * 用户列表(t_ele_tenant_map_key)表服务接口
 *
 * @author makejava
 * @since 2022-08-23 15:00:00
 */
public interface EleTenantMapKeyService extends IService<EleTenantMapKey> {

    R edit(EleTenantMapKeyAddAndUpdate eleTenantMapKeyAddAndUpdate);

    EleTenantMapKey queryFromCacheByTenantId(Integer tenantId);

}
