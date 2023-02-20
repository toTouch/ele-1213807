package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleNewOtaUpgradeHistory;
import com.xiliulou.electricity.mapper.EleNewOtaUpgradeHistoryMapper;
import com.xiliulou.electricity.service.EleNewOtaUpgradeHistoryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (EleNewOtaUpgradeHistory)表服务实现类
 *
 * @author Hardy
 * @since 2023-02-20 15:52:06
 */
@Service("eleNewOtaUpgradeHistoryService")
@Slf4j
public class EleNewOtaUpgradeHistoryServiceImpl implements EleNewOtaUpgradeHistoryService {
    
    @Resource
    private EleNewOtaUpgradeHistoryMapper eleNewOtaUpgradeHistoryMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleNewOtaUpgradeHistory queryByIdFromDB(Long id) {
        return this.eleNewOtaUpgradeHistoryMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleNewOtaUpgradeHistory queryByIdFromCache(Long id) {
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
    public List<EleNewOtaUpgradeHistory> queryAllByLimit(int offset, int limit) {
        return this.eleNewOtaUpgradeHistoryMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param eleNewOtaUpgradeHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleNewOtaUpgradeHistory insert(EleNewOtaUpgradeHistory eleNewOtaUpgradeHistory) {
        this.eleNewOtaUpgradeHistoryMapper.insertOne(eleNewOtaUpgradeHistory);
        return eleNewOtaUpgradeHistory;
    }
    
    /**
     * 修改数据
     *
     * @param eleNewOtaUpgradeHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleNewOtaUpgradeHistory eleNewOtaUpgradeHistory) {
        return this.eleNewOtaUpgradeHistoryMapper.update(eleNewOtaUpgradeHistory);
        
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
        return this.eleNewOtaUpgradeHistoryMapper.deleteById(id) > 0;
    }
}
