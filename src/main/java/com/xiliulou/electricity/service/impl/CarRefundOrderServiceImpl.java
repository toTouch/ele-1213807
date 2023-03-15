package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.CarRefundOrder;
import com.xiliulou.electricity.mapper.CarRefundOrderMapper;
import com.xiliulou.electricity.service.CarRefundOrderService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (CarRefundOrder)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-15 13:41:59
 */
@Service("carRefundOrderService")
@Slf4j
public class CarRefundOrderServiceImpl implements CarRefundOrderService {
    
    @Resource
    private CarRefundOrderMapper carRefundOrderMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarRefundOrder queryByIdFromDB(Long id) {
        return this.carRefundOrderMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarRefundOrder queryByIdFromCache(Long id) {
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
    public List<CarRefundOrder> queryAllByLimit(int offset, int limit) {
        return this.carRefundOrderMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param carRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarRefundOrder insert(CarRefundOrder carRefundOrder) {
        this.carRefundOrderMapper.insertOne(carRefundOrder);
        return carRefundOrder;
    }
    
    /**
     * 修改数据
     *
     * @param carRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarRefundOrder carRefundOrder) {
        return this.carRefundOrderMapper.update(carRefundOrder);
        
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
        return this.carRefundOrderMapper.deleteById(id) > 0;
    }
}
