package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleOtaUpgrade;
import com.xiliulou.electricity.mapper.EleOtaUpgradeMapper;
import com.xiliulou.electricity.service.EleOtaUpgradeService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleOtaUpgrade)表服务实现类
 *
 * @author Hardy
 * @since 2022-10-14 09:02:01
 */
@Service("eleOtaUpgradeService")
@Slf4j
public class EleOtaUpgradeServiceImpl implements EleOtaUpgradeService {
    
    @Resource
    private EleOtaUpgradeMapper eleOtaUpgradeMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaUpgrade queryByIdFromDB(Long id) {
        return this.eleOtaUpgradeMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleOtaUpgrade queryByIdFromCache(Long id) {
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
    public List<EleOtaUpgrade> queryAllByLimit(int offset, int limit) {
        return this.eleOtaUpgradeMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleOtaUpgrade insert(EleOtaUpgrade eleOtaUpgrade) {
        this.eleOtaUpgradeMapper.insertOne(eleOtaUpgrade);
        return eleOtaUpgrade;
    }
    
    /**
     * 修改数据
     *
     * @param eleOtaUpgrade 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleOtaUpgrade eleOtaUpgrade) {
        return this.eleOtaUpgradeMapper.update(eleOtaUpgrade);
        
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
        return this.eleOtaUpgradeMapper.deleteById(id) > 0;
    }
}
