package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.CarMemberCardOrder;
import com.xiliulou.electricity.mapper.CarMemberCardOrderMapper;
import com.xiliulou.electricity.service.CarMemberCardOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 租车套餐订单表(CarMemberCardOrder)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-21 09:47:25
 */
@Service("carMemberCardOrderService")
@Slf4j
public class CarMemberCardOrderServiceImpl implements CarMemberCardOrderService {
    @Autowired
    private CarMemberCardOrderMapper carMemberCardOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarMemberCardOrder selectByIdFromDB(Long id) {
        return this.carMemberCardOrderMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarMemberCardOrder selectByIdFromCache(Long id) {
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
    public List<CarMemberCardOrder> selectByPage(int offset, int limit) {
        return this.carMemberCardOrderMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarMemberCardOrder insert(CarMemberCardOrder carMemberCardOrder) {
        this.carMemberCardOrderMapper.insertOne(carMemberCardOrder);
        return carMemberCardOrder;
    }

    /**
     * 修改数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarMemberCardOrder carMemberCardOrder) {
        return this.carMemberCardOrderMapper.update(carMemberCardOrder);

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
        return this.carMemberCardOrderMapper.deleteById(id) > 0;
    }
}
