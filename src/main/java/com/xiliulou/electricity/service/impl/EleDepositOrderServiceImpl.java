package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.service.EleDepositOrderService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Service("tEleDepositOrderService")
public class EleDepositOrderServiceImpl implements EleDepositOrderService {
    @Resource
    private EleDepositOrderMapper eleDepositOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleDepositOrder queryByIdFromDB(Long id) {
        return this.eleDepositOrderMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleDepositOrder queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleDepositOrder insert(EleDepositOrder eleDepositOrder) {
        this.eleDepositOrderMapper.insert(eleDepositOrder);
        return eleDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleDepositOrder eleDepositOrder) {
       return this.eleDepositOrderMapper.update(eleDepositOrder);
         
    }
}