package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleAuthEntry;
import com.xiliulou.electricity.mapper.EleAuthEntryMapper;
import com.xiliulou.electricity.service.EleAuthEntryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 实名认证资料项(TEleAuthEntry)表服务实现类
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
@Service("tEleAuthEntryService")
public class EleAuthEntryServiceImpl implements EleAuthEntryService {
    @Resource
    private EleAuthEntryMapper eleAuthEntryMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleAuthEntry queryByIdFromDB(Long id) {
        return this.eleAuthEntryMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleAuthEntry queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleAuthEntry 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleAuthEntry insert(EleAuthEntry eleAuthEntry) {
        this.eleAuthEntryMapper.insert(eleAuthEntry);
        return eleAuthEntry;
    }

    /**
     * 修改数据
     *
     * @param eleAuthEntry 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleAuthEntry eleAuthEntry) {
       return this.eleAuthEntryMapper.update(eleAuthEntry);
         
    }
}