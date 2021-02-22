package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleRefundOrder;
import com.xiliulou.electricity.mapper.EleRefundOrderMapper;
import com.xiliulou.electricity.service.EleRefundOrderService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 退款订单表(TEleRefundOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:21:24
 */
@Service("tEleRefundOrderService")
public class EleRefundOrderServiceImpl implements EleRefundOrderService {
    @Resource
    private EleRefundOrderMapper eleRefundOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleRefundOrder queryByIdFromDB(Long id) {
        return this.eleRefundOrderMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  EleRefundOrder queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleRefundOrder insert(EleRefundOrder eleRefundOrder) {
        this.eleRefundOrderMapper.insert(eleRefundOrder);
        return eleRefundOrder;
    }

    /**
     * 修改数据
     *
     * @param eleRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleRefundOrder eleRefundOrder) {
       return this.eleRefundOrderMapper.update(eleRefundOrder);
         
    }
}