/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/7/23
 */

package com.xiliulou.electricity.service.impl.template;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.converter.TemplateConfigConverter;
import com.xiliulou.electricity.entity.TemplateConfigEntity;
import com.xiliulou.electricity.mapper.template.TemplateConfigMapper;
import com.xiliulou.electricity.request.template.TemplateConfigOptRequest;
import com.xiliulou.electricity.service.template.TemplateConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/7/23 15:28
 */
@Slf4j
@Service
public class TemplateConfigServiceImpl implements TemplateConfigService {
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private TemplateConfigMapper templateConfigMapper;
    
    
    @Override
    public TemplateConfigEntity queryByTenantIdAndChannelFromCache(Integer tenantId, String channel) {
        
        String key = this.buildKey(tenantId, channel);
        TemplateConfigEntity configEntity = redisService.getWithHash(key, TemplateConfigEntity.class);
        if (Objects.nonNull(configEntity)) {
            return configEntity;
        }
        
        TemplateConfigEntity templateConfig = templateConfigMapper.selectByChannelAndTenantId(channel, tenantId);
        if (Objects.isNull(templateConfig)) {
            return null;
        }
        redisService.saveWithString(key, templateConfig);
        
        return templateConfig;
    }
    
    @Override
    public List<String> queryTemplateIdByTenantIdChannel(Integer tenantId, String channel) {
        
        TemplateConfigEntity configEntity = this.queryByTenantIdAndChannelFromCache(tenantId, channel);
        if (Objects.isNull(configEntity)) {
            return Collections.emptyList();
        }
        
        List<String> tmpIds = new ArrayList<>();
        this.commonNotBlankAdd(tmpIds, configEntity.getBatteryOuttimeTemplate());
        this.commonNotBlankAdd(tmpIds, configEntity.getElectricQuantityRemindTemplate());
        this.commonNotBlankAdd(tmpIds, configEntity.getBatteryMemberCardExpiringTemplate());
        this.commonNotBlankAdd(tmpIds, configEntity.getCarMemberCardExpiringTemplate());
        return tmpIds;
    }
    
    
    @Override
    public R insert(TemplateConfigOptRequest request) {
        String channel = request.getChannel();
        Integer tenantId = request.getTenantId();
        
        TemplateConfigEntity exist = this.queryByTenantIdAndChannelFromCache(tenantId, channel);
        if (Objects.nonNull(exist)) {
            return R.fail("locker.10034", "已经有此租户的模板了，请勿重复添加");
        }
        
        TemplateConfigEntity configEntity = TemplateConfigConverter.optReqToDo(request);
        templateConfigMapper.insert(configEntity);
        return R.ok();
    }
    
    @Override
    public R update(TemplateConfigOptRequest request) {
        
        TemplateConfigEntity exist = templateConfigMapper.selectById(request.getTenantId(), request.getId());
        if (Objects.isNull(exist)) {
            return R.failMsg("数据不存在");
        }
        
        TemplateConfigEntity update = TemplateConfigConverter.optReqToDo(request);
        templateConfigMapper.update(update);
        redisService.delete(buildKey(request.getTenantId(), request.getChannel()));
        return R.ok();
    }
    
    @Override
    public R delete(Integer tenantId, Long id) {
        TemplateConfigEntity exist = templateConfigMapper.selectById(tenantId, id);
        if (Objects.isNull(exist)) {
            return R.failMsg("数据不存在");
        }
        templateConfigMapper.deleteById(tenantId, id);
        redisService.delete(buildKey(tenantId, exist.getChannel()));
        return R.ok();
    }
    
    /**
     * 构建缓存key
     *
     * @param tenantId
     * @param channel
     * @author caobotao.cbt
     * @date 2024/7/23 15:31
     */
    private String buildKey(Integer tenantId, String channel) {
        return String.format(CacheConstant.TEMPLATE_CONFIG_KEY, tenantId, channel);
    }
    
    
    /**
     * 非空添加
     *
     * @param tmpIds
     * @param value
     * @author caobotao.cbt
     * @date 2024/7/23 15:41
     */
    private void commonNotBlankAdd(List<String> tmpIds, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        tmpIds.add(value);
    }
}
