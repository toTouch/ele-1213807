package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.TElectricityBattery;
import com.xiliulou.electricity.mapper.TElectricityBatteryMapper;
import com.xiliulou.electricity.service.TElectricityBatteryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜电池表(TElectricityBattery)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 14:44:12
 */
@Service("tElectricityBatteryService")
public class TElectricityBatteryServiceImpl implements TElectricityBatteryService {
    @Resource
    private TElectricityBatteryMapper tElectricityBatteryMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TElectricityBattery queryByIdFromDB(Long id) {
        return this.tElectricityBatteryMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  TElectricityBattery queryByIdFromCache(Long id) {
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
    public List<TElectricityBattery> queryAllByLimit(int offset, int limit) {
        return this.tElectricityBatteryMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param tElectricityBattery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TElectricityBattery insert(TElectricityBattery tElectricityBattery) {
        this.tElectricityBatteryMapper.insert(tElectricityBattery);
        return tElectricityBattery;
    }

    /**
     * 修改数据
     *
     * @param tElectricityBattery 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(TElectricityBattery tElectricityBattery) {
       return this.tElectricityBatteryMapper.update(tElectricityBattery);
         
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
        return this.tElectricityBatteryMapper.deleteById(id) > 0;
    }
}