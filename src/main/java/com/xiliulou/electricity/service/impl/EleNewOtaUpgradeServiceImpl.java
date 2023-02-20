package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleNewOtaUpgrade;
import com.xiliulou.electricity.mapper.EleNewOtaUpgradeMapper;
import com.xiliulou.electricity.service.EleNewOtaUpgradeService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleNewOtaUpgrade)表服务实现类
 *
 * @author Hardy
 * @since 2023-02-20 15:58:55
 */
@Service("eleNewOtaUpgradeService")
@Slf4j
public class EleNewOtaUpgradeServiceImpl implements EleNewOtaUpgradeService {
    
    @Resource
    private EleNewOtaUpgradeMapper eleNewOtaUpgradeMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleNewOtaUpgrade queryByIdFromDB(Long id) {
        return this.eleNewOtaUpgradeMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleNewOtaUpgrade queryByIdFromCache(Long id) {
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
    public List<EleNewOtaUpgrade> queryAllByLimit(int offset, int limit) {
        return this.eleNewOtaUpgradeMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleNewOtaUpgrade insert(EleNewOtaUpgrade eleNewOtaUpgrade) {
        this.eleNewOtaUpgradeMapper.insertOne(eleNewOtaUpgrade);
        return eleNewOtaUpgrade;
    }
    
    /**
     * 修改数据
     *
     * @param eleNewOtaUpgrade 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleNewOtaUpgrade eleNewOtaUpgrade) {
        return this.eleNewOtaUpgradeMapper.update(eleNewOtaUpgrade);
        
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
        return this.eleNewOtaUpgradeMapper.deleteById(id) > 0;
    }
}
