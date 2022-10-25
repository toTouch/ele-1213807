package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.mapper.TemplateConfigMapper;
import com.xiliulou.electricity.service.TemplateConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Hardy
 * @date 2021/11/30 19:27
 * @mood
 */
@Service("templateConfigService")
public class TemplateConfigServiceImpl extends ServiceImpl<TemplateConfigMapper, TemplateConfigEntity> implements TemplateConfigService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private TemplateConfigMapper templateConfigMapper;

    @Override
    public TemplateConfigEntity queryByTenantIdFromCache(Integer tenantId) {
        TemplateConfigEntity templateConfigFromCache = redisService.getWithHash(CacheConstant.CACHE_TEMPLATE_CONFIG + tenantId, TemplateConfigEntity.class);
        if (Objects.nonNull(templateConfigFromCache)) {
            return templateConfigFromCache;
        }

        TemplateConfigEntity templateConfigFromDB = this.queryByTenantIdFromDB(tenantId);
        if (Objects.isNull(templateConfigFromDB)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_TEMPLATE_CONFIG + tenantId, templateConfigFromDB);
        return templateConfigFromDB;
    }

    @Override
    public R queryByTenantId() {
        Integer tenantId = TenantContextHolder.getTenantId();
        TemplateConfigEntity templateConfigEntity = queryByTenantIdFromCache(tenantId);
        return R.ok(templateConfigEntity);
    }

    @Override
    public R saveDBandCache(TemplateConfigEntity templateConfigEntity) {
        Integer tenantId = TenantContextHolder.getTenantId();

        TemplateConfigEntity templateConfigEntityFromCache = this.queryByTenantIdFromCache(tenantId);
        if (Objects.nonNull(templateConfigEntityFromCache)) {
            return R.fail("locker.10034", "已经有此租户的模板了，请勿重复添加");
        }

        templateConfigEntity.setTenantId(tenantId);
        templateConfigEntity.setCreateTime(System.currentTimeMillis());
        templateConfigEntity.setUpdateTime(System.currentTimeMillis());
        int insert = this.baseMapper.insert(templateConfigEntity);
        if (insert > 0) {
            redisService.saveWithHash(CacheConstant.CACHE_TEMPLATE_CONFIG + tenantId, templateConfigEntity);
        }
        return R.ok();
    }

    @Override
    public R updateByIdFromDB(TemplateConfigEntity templateConfig) {
        if (Objects.isNull(templateConfig.getId())) {
            return R.fail("id不能为空");
        }
        templateConfig.setUpdateTime(System.currentTimeMillis());
        templateConfig.setTenantId(TenantContextHolder.getTenantId());

        int i = templateConfigMapper.update(templateConfig);
        if (i > 0) {
            redisService.delete(CacheConstant.CACHE_TEMPLATE_CONFIG + TenantContextHolder.getTenantId());
        }
        return R.ok();
    }

    @Override
    public R removeByIdFromDB(Long id) {
        int i = templateConfigMapper.deleteById(id,TenantContextHolder.getTenantId());
        if (i > 0) {
            redisService.delete(CacheConstant.CACHE_TEMPLATE_CONFIG + TenantContextHolder.getTenantId());
        }
        return R.ok();
    }

    @Override
    public TemplateConfigEntity queryByTenantIdFromDB(Integer tenantId) {
        LambdaQueryWrapper<TemplateConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateConfigEntity::getTenantId, tenantId);

        TemplateConfigEntity templateConfigEntity = this.baseMapper.selectOne(wrapper);
        return templateConfigEntity;
    }

    @Override
    public R queryTemplateId() {
        Integer tenantId = TenantContextHolder.getTenantId();
        List<String> result = new ArrayList<>(3);

        TemplateConfigEntity templateConfigEntity = queryByTenantIdFromCache(tenantId);
        if(Objects.nonNull(templateConfigEntity)){
            result.add(templateConfigEntity.getBatteryOuttimeTemplate());
            result.add(templateConfigEntity.getElectricQuantityRemindTemplate());
            //result.add(templateConfigEntity.getMemberCardExpiringTemplate());
        }

        return R.ok(result);
    }

}
