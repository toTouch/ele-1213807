package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleParamSettingTemplate;
import com.xiliulou.electricity.mapper.EleParamSettingTemplateMapper;
import com.xiliulou.electricity.service.EleParamSettingTemplateService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
}
