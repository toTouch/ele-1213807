package com.xiliulou.electricity.service.enterprise;


import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanDetail;

import java.util.List;

/**
 * 企业云豆详情表(TEnterpriseCloudBeanDetail)表服务接口
 *
 * @author makejava
 * @since 2025-01-15 19:18:06
 */
public interface EnterpriseCloudBeanDetailService {

    EnterpriseCloudBeanDetail queryByEnterpriseId(Long id);
}
