package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.RentCarOrder;
import com.xiliulou.electricity.mapper.RentCarOrderMapper;
import com.xiliulou.electricity.service.RentCarOrderService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 租车记录(TRentCarOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@Service("tRentCarOrderService")
public class RentCarOrderServiceImpl implements RentCarOrderService {
    @Resource
    private RentCarOrderMapper rentCarOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public RentCarOrder queryByIdFromDB(Long id) {
        return this.rentCarOrderMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public RentCarOrder queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentCarOrder insert(RentCarOrder rentCarOrder) {
        this.rentCarOrderMapper.insert(rentCarOrder);
        return rentCarOrder;
    }

    /**
     * 修改数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(RentCarOrder rentCarOrder) {
       return this.rentCarOrderMapper.update(rentCarOrder);
         
    }
}