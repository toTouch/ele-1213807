package com.xiliulou.electricity.service.impl.enterprise;


import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOverview;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseCloudBeanOverviewMapper;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOverviewService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 企业云豆总览表(TEnterpriseCloudBeanOverview)表服务实现类
 *
 * @author mxd
 * @since 2025-01-15 19:56:24
 */
@Service
public class EnterpriseCloudBeanOverviewServiceImpl implements EnterpriseCloudBeanOverviewService {
    @Resource
    private EnterpriseCloudBeanOverviewMapper enterpriseCloudBeanOverviewMapper;

    @Override
    @Slave
    public EnterpriseCloudBeanOverview queryByEnterpriseId(Long id) {
        return enterpriseCloudBeanOverviewMapper.selectByEnterpriseId(id);
    }
}
