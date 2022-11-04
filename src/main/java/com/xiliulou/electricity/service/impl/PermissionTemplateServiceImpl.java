package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.PermissionTemplate;
import com.xiliulou.electricity.mapper.PermissionTemplateMapper;
import com.xiliulou.electricity.query.PermissionTemplateQuery;
import com.xiliulou.electricity.service.PermissionTemplateService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * (PermissionTemplate)表服务实现类
 *
 * @author zzlong
 * @since 2022-09-19 16:34:06
 */
@Service("permissionTemplateService")
@Slf4j
public class PermissionTemplateServiceImpl implements PermissionTemplateService {
    @Autowired
    private PermissionTemplateMapper permissionTemplateMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public PermissionTemplate selectByIdFromDB(Long id) {
        return this.permissionTemplateMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public PermissionTemplate selectByIdFromCache(Long id) {
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
    public List<PermissionTemplate> selectByPage(int offset, int limit) {
        return this.permissionTemplateMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param permissionTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionTemplate insert(PermissionTemplate permissionTemplate) {
        this.permissionTemplateMapper.insertOne(permissionTemplate);
        return permissionTemplate;
    }

    /**
     * 修改数据
     *
     * @param permissionTemplate 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(PermissionTemplate permissionTemplate) {
        return this.permissionTemplateMapper.update(permissionTemplate);

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
        return this.permissionTemplateMapper.deleteById(id) > 0;
    }

    @Override
    public List<Long> selectByType(Integer typeOperate) {
        List<PermissionTemplate> permissionTemplates = this.permissionTemplateMapper.selectList(new LambdaQueryWrapper<PermissionTemplate>().eq(PermissionTemplate::getType, typeOperate));
        if(CollectionUtils.isEmpty(permissionTemplates)){
            return Collections.EMPTY_LIST;
        }

        return permissionTemplates.stream().map(PermissionTemplate::getPid).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPermissionTemplate(PermissionTemplateQuery permissionTemplateQuery) {

        //删除旧权限
        permissionTemplateMapper.deleteByType(permissionTemplateQuery.getType());

        //保存新权限
        if(CollectionUtils.isEmpty(permissionTemplateQuery.getPermissionIds())){
            return 0;
        }

        List<PermissionTemplate> permissionList = permissionTemplateQuery.getPermissionIds().parallelStream().map(item -> {
            PermissionTemplate permissionTemplate = new PermissionTemplate();
            permissionTemplate.setPid(item);
            permissionTemplate.setType(permissionTemplateQuery.getType());
            return permissionTemplate;
        }).collect(Collectors.toList());

        return permissionTemplateMapper.batchInsert(permissionList);
    }
}
