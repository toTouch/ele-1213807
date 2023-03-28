package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleParamSettingTemplate;
import com.xiliulou.electricity.mapper.EleParamSettingTemplateMapper;
import com.xiliulou.electricity.query.EleParamSettingTemplateBatchSettingQuery;
import com.xiliulou.electricity.query.EleParamSettingTemplateQuery;
import com.xiliulou.electricity.service.EleParamSettingTemplateService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleParamSettingTemplate)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-28 09:53:19
 */
@Service("eleParamSettingTemplateService")
@Slf4j
public class EleParamSettingTemplateServiceImpl implements EleParamSettingTemplateService {
    
    @Resource
    private EleParamSettingTemplateMapper eleParamSettingTemplateMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleParamSettingTemplate queryByIdFromDB(Long id) {
        return this.eleParamSettingTemplateMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleParamSettingTemplate queryByIdFromCache(Long id) {
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
    public List<EleParamSettingTemplate> queryAllByLimit(int offset, int limit) {
        return this.eleParamSettingTemplateMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleParamSettingTemplate insert(EleParamSettingTemplate eleParamSettingTemplate) {
        this.eleParamSettingTemplateMapper.insertOne(eleParamSettingTemplate);
        return eleParamSettingTemplate;
    }
    
    /**
     * 修改数据
     *
     * @param eleParamSettingTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleParamSettingTemplate eleParamSettingTemplate) {
        return this.eleParamSettingTemplateMapper.update(eleParamSettingTemplate);
        
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
        return this.eleParamSettingTemplateMapper.deleteById(id) > 0;
    }
    
    @Override
    public Triple<Boolean, String, Object> queryList(Long offset, Long size, String name) {
        List<EleParamSettingTemplate> templates = eleParamSettingTemplateMapper.queryList(offset, size, name);
        return Triple.of(true, null, templates);
    }
    
    @Override
    public Triple<Boolean, String, Object> queryCount(String name) {
        Long count = eleParamSettingTemplateMapper.queryCount(name);
        return Triple.of(true, null, count);
    }
    
    @Override
    public Triple<Boolean, String, Object> deleteOne(Long id) {
        EleParamSettingTemplate update = new EleParamSettingTemplate();
        update.setId(id);
        update.setDelFlag(EleParamSettingTemplate.DEL_DEL);
        update.setUpdateTime(System.currentTimeMillis());
        eleParamSettingTemplateMapper.update(update);
        return Triple.of(true, "", "");
    }
    
    @Override
    public Triple<Boolean, String, Object> saveOne(EleParamSettingTemplateQuery eleParamSettingTemplateQuery) {
        EleParamSettingTemplate save = new EleParamSettingTemplate();
        BeanUtils.copyProperties(eleParamSettingTemplateQuery, save);
        save.setCreateTime(System.currentTimeMillis());
        save.setUpdateTime(System.currentTimeMillis());
        save.setDelFlag(EleParamSettingTemplate.DEL_NORMAL);
        save.setTenantId(TenantContextHolder.getTenantId());
        eleParamSettingTemplateMapper.insertOne(save);
        return Triple.of(true, "", "");
    }
    
    @Override
    public Triple<Boolean, String, Object> updateOne(EleParamSettingTemplateQuery eleParamSettingTemplateQuery) {
        EleParamSettingTemplate eleParamSettingTemplate = eleParamSettingTemplateMapper
                .queryById(eleParamSettingTemplateQuery.getId());
        if (Objects.isNull(eleParamSettingTemplate)) {
            return Triple.of(false, "", "未找到参数模板");
        }
        
        if (!Objects.equals(eleParamSettingTemplate.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(true, "", "");
        }
        
        EleParamSettingTemplate update = new EleParamSettingTemplate();
        BeanUtils.copyProperties(eleParamSettingTemplateQuery, update);
        update.setUpdateTime(System.currentTimeMillis());
        eleParamSettingTemplateMapper.update(update);
        return Triple.of(true, "", "");
    }
    
}
