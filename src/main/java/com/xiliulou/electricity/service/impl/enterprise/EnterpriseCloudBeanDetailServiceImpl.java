package com.xiliulou.electricity.service.impl.enterprise;


import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanDetail;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseCloudBeanDetailMapper;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 企业云豆详情表(TEnterpriseCloudBeanDetail)表服务实现类
 *
 * @author makejava
 * @since 2025-01-15 19:18:06
 */
@Service
public class EnterpriseCloudBeanDetailServiceImpl implements EnterpriseCloudBeanDetailService {
    @Resource
    private EnterpriseCloudBeanDetailMapper enterpriseCloudBeanDetailMapper;

    @Override
    @Slave
    public EnterpriseCloudBeanDetail queryByEnterpriseId(Long id) {
        return enterpriseCloudBeanDetailMapper.selectByEnterpriseId(id);
    }
}
