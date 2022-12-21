package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.CarDepositOrder;
import com.xiliulou.electricity.mapper.CarDepositOrderMapper;
import com.xiliulou.electricity.service.CarDepositOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (CarDepositOrder)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-21 09:15:22
 */
@Service("carDepositOrderService")
@Slf4j
public class CarDepositOrderServiceImpl implements CarDepositOrderService {
    @Autowired
    private CarDepositOrderMapper carDepositOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarDepositOrder selectByIdFromDB(Long id) {
        return this.carDepositOrderMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarDepositOrder selectByIdFromCache(Long id) {
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
    public List<CarDepositOrder> selectByPage(int offset, int limit) {
        return this.carDepositOrderMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param carDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarDepositOrder insert(CarDepositOrder carDepositOrder) {
        this.carDepositOrderMapper.insertOne(carDepositOrder);
        return carDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param carDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarDepositOrder carDepositOrder) {
        return this.carDepositOrderMapper.update(carDepositOrder);

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
        return this.carDepositOrderMapper.deleteById(id) > 0;
    }
}
