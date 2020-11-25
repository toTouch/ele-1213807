package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetModelMapper;
import com.xiliulou.electricity.service.ElectricityCabinetModelService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜型号表(TElectricityCabinetModel)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:01:04
 */
@Service("electricityCabinetModelService")
public class ElectricityCabinetModelServiceImpl implements ElectricityCabinetModelService {
    @Resource
    private ElectricityCabinetModelMapper electricityCabinetModelMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetModel queryByIdFromDB(Integer id) {
        return this.electricityCabinetModelMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetModel queryByIdFromCache(Integer id) {
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
    public List<ElectricityCabinetModel> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetModelMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetModel insert(ElectricityCabinetModel electricityCabinetModel) {
        this.electricityCabinetModelMapper.insert(electricityCabinetModel);
        return electricityCabinetModel;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetModel electricityCabinetModel) {
       return this.electricityCabinetModelMapper.update(electricityCabinetModel);
         
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
        return this.electricityCabinetModelMapper.deleteById(id) > 0;
    }
}