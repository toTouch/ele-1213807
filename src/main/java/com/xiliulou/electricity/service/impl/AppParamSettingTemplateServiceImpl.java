package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.AppParamSettingTemplate;
import com.xiliulou.electricity.mapper.AppParamSettingTemplateMapper;
import com.xiliulou.electricity.query.AppParamSettingTemplateQuery;
import com.xiliulou.electricity.service.AppParamSettingTemplateService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (AppParamSettingTemplate)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-30 19:53:10
 */
@Service("appParamSettingTemplateService")
@Slf4j
public class AppParamSettingTemplateServiceImpl implements AppParamSettingTemplateService {
    
    @Resource
    private AppParamSettingTemplateMapper appParamSettingTemplateMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AppParamSettingTemplate queryByIdFromDB(Long id) {
        return this.appParamSettingTemplateMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public AppParamSettingTemplate queryByIdFromCache(Long id) {
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
    public List<AppParamSettingTemplate> queryAllByLimit(int offset, int limit) {
        return this.appParamSettingTemplateMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param appParamSettingTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppParamSettingTemplate insert(AppParamSettingTemplate appParamSettingTemplate) {
        this.appParamSettingTemplateMapper.insertOne(appParamSettingTemplate);
        return appParamSettingTemplate;
    }
    
    /**
     * 修改数据
     *
     * @param appParamSettingTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(AppParamSettingTemplate appParamSettingTemplate) {
        return this.appParamSettingTemplateMapper.update(appParamSettingTemplate);
        
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
        return this.appParamSettingTemplateMapper.deleteById(id) > 0;
    }
    
    @Override
    public R queryList(Long size, Long offset) {
        List<AppParamSettingTemplate> queryList = this.appParamSettingTemplateMapper
                .queryList(size, offset, TenantContextHolder.getTenantId());
        return R.ok(queryList);
    }
    
    @Override
    public R queryCount() {
        Long count = this.appParamSettingTemplateMapper.queryCount(TenantContextHolder.getTenantId());
        return R.ok(count);
    }
    
    @Override
    public R deleteOne(Long id) {
        AppParamSettingTemplate appParamSettingTemplate = new AppParamSettingTemplate();
        appParamSettingTemplate.setId(id);
        appParamSettingTemplate.setDelFlag(AppParamSettingTemplate.DEL_DEL);
        appParamSettingTemplate.setUpdateTime(System.currentTimeMillis());
        this.appParamSettingTemplateMapper.update(appParamSettingTemplate);
        return R.ok();
    }
    
    @Override
    public R saveOne(AppParamSettingTemplateQuery query) {
        AppParamSettingTemplate appParamSettingTemplate = new AppParamSettingTemplate();
        appParamSettingTemplate.setName(query.getName());
        appParamSettingTemplate.setConfigContent(query.getConfigContent());
        appParamSettingTemplate.setCreateTime(System.currentTimeMillis());
        appParamSettingTemplate.setUpdateTime(System.currentTimeMillis());
        appParamSettingTemplate.setDelFlag(AppParamSettingTemplate.DEL_NORMAL);
        appParamSettingTemplate.setTenantId(TenantContextHolder.getTenantId());
        appParamSettingTemplateMapper.insertOne(appParamSettingTemplate);
        return R.ok();
    }
    
    @Override
    public R updateOne(AppParamSettingTemplateQuery query) {
        AppParamSettingTemplate template = this.queryByIdFromDB(query.getId());
        if (Objects.isNull(template)) {
            log.error("AppParamSettingTemplate ERROR! template not find! id={}", query.getId());
            return R.fail("100450", "未查询到APP参数模板");
        }
        
        if (!Objects.equals(TenantContextHolder.getTenantId(), template.getTenantId())) {
            return R.ok();
        }
        
        AppParamSettingTemplate appParamSettingTemplate = new AppParamSettingTemplate();
        appParamSettingTemplate.setId(query.getId());
        appParamSettingTemplate.setName(query.getName());
        appParamSettingTemplate.setConfigContent(query.getConfigContent());
        appParamSettingTemplate.setUpdateTime(System.currentTimeMillis());
        appParamSettingTemplateMapper.update(appParamSettingTemplate);
        return R.ok();
    }
}
