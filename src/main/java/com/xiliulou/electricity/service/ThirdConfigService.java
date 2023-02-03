package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ThirdConfig;
import com.xiliulou.electricity.query.ThirdConfigQuery;

import java.util.List;

/**
 * (ThirdConfig)表服务接口
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
public interface ThirdConfigService {

    ThirdConfig selectLatestByTenantId(Integer tenantId);


    Integer insertOrUpdate(ThirdConfigQuery thirdConfigQuery);
}
