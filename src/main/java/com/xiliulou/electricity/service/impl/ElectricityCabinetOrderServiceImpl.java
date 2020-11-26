package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
import com.xiliulou.electricity.service.ElectricityCabinetOrderService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@Service("electricityCabinetOrderService")
public class ElectricityCabinetOrderServiceImpl implements ElectricityCabinetOrderService {
    @Resource
    private ElectricityCabinetOrderMapper electricityCabinetOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrder queryByIdFromDB(Long id) {
        return this.electricityCabinetOrderMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrder queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetOrder> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetOrderMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetOrder insert(ElectricityCabinetOrder electricityCabinetOrder) {
        this.electricityCabinetOrderMapper.insert(electricityCabinetOrder);
        return electricityCabinetOrder;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetOrder electricityCabinetOrder) {
        return this.electricityCabinetOrderMapper.update(electricityCabinetOrder);

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
        return this.electricityCabinetOrderMapper.deleteById(id) > 0;
    }


    /*
      1.判断参数
      2.判断用户是否有电池是否有月卡
      3.生成订单
      4.开旧电池门
      5.旧电池门开回调
      6.旧电池门关回调
      7.旧电池检测回调
      8.检测失败重复开门
      9.检测成功开新电池门
      10.新电池开门回调
      11.新电池关门回调
      */
    @Override
    public R order(ElectricityCabinetOrder electricityCabinetOrder) {
        //TODO 1.判断参数
        //TODO 2.判断用户是否有电池是否有月卡
        //TODO 3.生成订单
        //TODO 4.开旧电池门
        return null;
    }
}