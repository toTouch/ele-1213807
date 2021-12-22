package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.TemplateConfigEntity;

/**
 * @author zgw
 * @date 2021/11/30 19:25
 * @mood
 */
public interface TemplateConfigService extends IService<TemplateConfigEntity> {
    TemplateConfigEntity queryByTenantIdFromCache(Integer tenantId);

    R queryByTenantId();

    R saveDBandCache(TemplateConfigEntity templateConfigEntity );

    R updateByIdFromDB(TemplateConfigEntity templateConfig);

    R removeByIdFromDB(Long id);

    TemplateConfigEntity queryByTenantIdFromDB(Integer tenantId);

    R queryTemplateId();
}
