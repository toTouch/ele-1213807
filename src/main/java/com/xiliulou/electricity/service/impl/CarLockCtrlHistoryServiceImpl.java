package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.CarLockCtrlHistory;
import com.xiliulou.electricity.mapper.CarLockCtrlHistoryMapper;
import com.xiliulou.electricity.service.CarLockCtrlHistoryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (CarLockCtrlHistory)表服务实现类
 *
 * @author Hardy
 * @since 2023-04-04 16:22:29
 */
@Service("carLockCtrlHistoryService")
@Slf4j
public class CarLockCtrlHistoryServiceImpl implements CarLockCtrlHistoryService {
    
    @Resource
    private CarLockCtrlHistoryMapper carLockCtrlHistoryMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarLockCtrlHistory queryByIdFromDB(Long id) {
        return this.carLockCtrlHistoryMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarLockCtrlHistory queryByIdFromCache(Long id) {
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
    public List<CarLockCtrlHistory> queryAllByLimit(int offset, int limit) {
        return this.carLockCtrlHistoryMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param carLockCtrlHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarLockCtrlHistory insert(CarLockCtrlHistory carLockCtrlHistory) {
        this.carLockCtrlHistoryMapper.insertOne(carLockCtrlHistory);
        return carLockCtrlHistory;
    }
    
    /**
     * 修改数据
     *
     * @param carLockCtrlHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarLockCtrlHistory carLockCtrlHistory) {
        return this.carLockCtrlHistoryMapper.update(carLockCtrlHistory);
        
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
        return this.carLockCtrlHistoryMapper.deleteById(id) > 0;
    }
}
