package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.BoxOtherProperties;
import com.xiliulou.electricity.mapper.BoxOtherPropertiesMapper;
import com.xiliulou.electricity.query.BoxOtherPropertiesQuery;
import com.xiliulou.electricity.service.BoxOtherPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 换电柜仓门其它属性(BoxOtherProperties)表服务实现类
 *
 * @author zzlong
 * @since 2022-11-03 19:51:35
 */
@Service("boxOtherPropertiesService")
@Slf4j
public class BoxOtherPropertiesServiceImpl implements BoxOtherPropertiesService {
    
    @Autowired
    private BoxOtherPropertiesMapper boxOtherPropertiesMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BoxOtherProperties selectByIdFromDB(Long id) {
        return this.boxOtherPropertiesMapper.selectById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BoxOtherProperties selectByIdFromCache(Long id) {
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
    public List<BoxOtherProperties> selectByPage(int offset, int limit) {
        return this.boxOtherPropertiesMapper.selectByPage(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param boxOtherProperties 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BoxOtherProperties insert(BoxOtherProperties boxOtherProperties) {
        boxOtherProperties.setCreateTime(System.currentTimeMillis());
        boxOtherProperties.setUpdateTime(System.currentTimeMillis());
        boxOtherProperties.setDelFlag(BoxOtherProperties.DEL_NORMAL);
        return this.boxOtherPropertiesMapper.insertBoxOtherProperties(boxOtherProperties);
    }
    
    /**
     * 修改数据
     *
     * @param boxOtherProperties 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BoxOtherProperties boxOtherProperties) {
        return this.boxOtherPropertiesMapper.update(boxOtherProperties);
        
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
        return this.boxOtherPropertiesMapper.deleteById(id) > 0;
    }
}
