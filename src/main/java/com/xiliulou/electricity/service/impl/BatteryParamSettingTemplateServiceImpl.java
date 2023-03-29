package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.BatteryParamSettingTemplate;
import com.xiliulou.electricity.mapper.BatteryParamSettingTemplateMapper;
import com.xiliulou.electricity.query.BatteryParamSettingTemplateQuery;
import com.xiliulou.electricity.service.BatteryParamSettingTemplateService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (BatteryParamSettingTemplate)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-29 09:20:23
 */
@Service("batteryParamSettingTemplateService")
@Slf4j
public class BatteryParamSettingTemplateServiceImpl implements BatteryParamSettingTemplateService {
    
    @Resource
    private BatteryParamSettingTemplateMapper batteryParamSettingTemplateMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryParamSettingTemplate queryByIdFromDB(Long id) {
        return this.batteryParamSettingTemplateMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryParamSettingTemplate queryByIdFromCache(Long id) {
        return null;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<BatteryParamSettingTemplate> queryAllByLimit(int offset, int limit) {
        return this.batteryParamSettingTemplateMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryParamSettingTemplate insert(BatteryParamSettingTemplate batteryParamSettingTemplate) {
        this.batteryParamSettingTemplateMapper.insertOne(batteryParamSettingTemplate);
        return batteryParamSettingTemplate;
    }
    
    /**
     * 修改数据
     *
     * @param batteryParamSettingTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryParamSettingTemplate batteryParamSettingTemplate) {
        return this.batteryParamSettingTemplateMapper.update(batteryParamSettingTemplate);
        
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.batteryParamSettingTemplateMapper.deleteById(id) > 0;
    }
    
    @Override
    public Triple<Boolean, String, Object> queryList(Long offset, Long size, String name) {
        List<BatteryParamSettingTemplate> queryList = batteryParamSettingTemplateMapper
                .queryList(offset, size, name, TenantContextHolder.getTenantId());
        return Triple.of(true, "", queryList);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryCount(String name) {
        Long count = batteryParamSettingTemplateMapper.queryCount(name, TenantContextHolder.getTenantId());
        return Triple.of(true, "", count);
    }
    
    @Override
    public Triple<Boolean, String, Object> deleteOne(Long id) {
        BatteryParamSettingTemplate template = queryByIdFromDB(id);
        if (Objects.isNull(template)) {
            return Triple.of(false, "100450", "未查询到电池参数模板");
        }
        
        if (Objects.equals(template.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, "", "");
        }
        
        BatteryParamSettingTemplate delete = new BatteryParamSettingTemplate();
        delete.setId(id);
        delete.setDelFlag(BatteryParamSettingTemplate.DEL_DEL);
        delete.setUpdateTime(System.currentTimeMillis());
        this.update(delete);
        return Triple.of(true, "", "");
    }
    
    @Override
    public Triple<Boolean, String, Object> saveOne(BatteryParamSettingTemplateQuery query) {
        
        BatteryParamSettingTemplate save = new BatteryParamSettingTemplate();
        BeanUtils.copyProperties(query, save);
        save.setCreateTime(System.currentTimeMillis());
        save.setUpdateTime(System.currentTimeMillis());
        save.setTenantId(TenantContextHolder.getTenantId());
        save.setDelFlag(BatteryParamSettingTemplate.DEL_NORMAL);
        this.insert(save);
        return Triple.of(true, "", "");
    }
    
    @Override
    public Triple<Boolean, String, Object> updateOne(BatteryParamSettingTemplateQuery query) {
        BatteryParamSettingTemplate template = queryByIdFromDB(query.getId());
        if (Objects.isNull(template)) {
            return Triple.of(false, "100450", "未查询到电池参数模板");
        }
        
        if (Objects.equals(template.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, "", "");
        }
        
        BatteryParamSettingTemplate update = new BatteryParamSettingTemplate();
        BeanUtils.copyProperties(query, update);
        update.setUpdateTime(System.currentTimeMillis());
        this.update(update);
        
        return Triple.of(true, "", "");
    }
}
