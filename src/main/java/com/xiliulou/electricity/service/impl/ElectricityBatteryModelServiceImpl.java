package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityBatteryModel;
import com.xiliulou.electricity.mapper.ElectricityBatteryModelMapper;
import com.xiliulou.electricity.service.ElectricityBatteryModelService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 电池型号(ElectricityBatteryModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Service("ElectricityBatteryModelService")
public class ElectricityBatteryModelServiceImpl implements ElectricityBatteryModelService {
    @Resource
    private ElectricityBatteryModelMapper ElectricityBatteryModelMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityBatteryModel queryByIdFromDB(Integer id) {
        return this.ElectricityBatteryModelMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityBatteryModel queryByIdFromCache(Integer id) {
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
    public List<ElectricityBatteryModel> queryAllByLimit(int offset, int limit) {
        return this.ElectricityBatteryModelMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityBatteryModel insert(ElectricityBatteryModel ElectricityBatteryModel) {
        this.ElectricityBatteryModelMapper.insert(ElectricityBatteryModel);
        return ElectricityBatteryModel;
    }

    /**
     * 修改数据
     *
     * @param ElectricityBatteryModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityBatteryModel ElectricityBatteryModel) {
        return this.ElectricityBatteryModelMapper.update(ElectricityBatteryModel);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        return this.ElectricityBatteryModelMapper.deleteById(id) > 0;
    }
}