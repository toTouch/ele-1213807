package com.xiliulou.electricity.service.enterprise;



import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOverview;

import java.util.List;

/**
 * 企业云豆总览表(TEnterpriseCloudBeanOverview)表服务接口
 *
 * @author mxd
 * @since 2025-01-15 19:56:24
 */
public interface EnterpriseCloudBeanOverviewService {

    EnterpriseCloudBeanOverview queryByEnterpriseId(Long id);
}
