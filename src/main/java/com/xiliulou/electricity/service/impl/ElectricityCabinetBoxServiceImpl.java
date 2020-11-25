package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxMapper;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Service("electricityCabinetBoxService")
public class ElectricityCabinetBoxServiceImpl implements ElectricityCabinetBoxService {
    @Resource
    private ElectricityCabinetBoxMapper electricityCabinetBoxMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromDB(Long id) {
        return this.electricityCabinetBoxMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetBox> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetBoxMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetBox insert(ElectricityCabinetBox electricityCabinetBox) {
        this.electricityCabinetBoxMapper.insert(electricityCabinetBox);
        return electricityCabinetBox;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetBox electricityCabinetBox) {
       return this.electricityCabinetBoxMapper.update(electricityCabinetBox);
         
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
        return this.electricityCabinetBoxMapper.deleteById(id) > 0;
    }

    @Override
    public void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id) {

    }
}