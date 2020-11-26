package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.TElectricityBatteryModel;
import com.xiliulou.electricity.mapper.TElectricityBatteryModelMapper;
import com.xiliulou.electricity.service.TElectricityBatteryModelService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 电池型号(TElectricityBatteryModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:44
 */
@Service("tElectricityBatteryModelService")
public class TElectricityBatteryModelServiceImpl implements TElectricityBatteryModelService {
    @Resource
    private TElectricityBatteryModelMapper tElectricityBatteryModelMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TElectricityBatteryModel queryByIdFromDB(Integer id) {
        return this.tElectricityBatteryModelMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  TElectricityBatteryModel queryByIdFromCache(Integer id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<TElectricityBatteryModel> queryAllByLimit(int offset, int limit) {
        return this.tElectricityBatteryModelMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param tElectricityBatteryModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TElectricityBatteryModel insert(TElectricityBatteryModel tElectricityBatteryModel) {
        this.tElectricityBatteryModelMapper.insert(tElectricityBatteryModel);
        return tElectricityBatteryModel;
    }

    /**
     * 修改数据
     *
     * @param tElectricityBatteryModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(TElectricityBatteryModel tElectricityBatteryModel) {
       return this.tElectricityBatteryModelMapper.update(tElectricityBatteryModel);
         
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
        return this.tElectricityBatteryModelMapper.deleteById(id) > 0;
    }
}