package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.DivisionAccountCarModel;
import com.xiliulou.electricity.mapper.DivisionAccountCarModelMapper;
import com.xiliulou.electricity.service.DivisionAccountCarModelService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (DivisionAccountCarModel)表服务实现类
 *
 * @author zzlong
 * @since 2023-04-23 18:00:15
 */
@Service("divisionAccountCarModelService")
@Slf4j
public class DivisionAccountCarModelServiceImpl implements DivisionAccountCarModelService {
    @Resource
    private DivisionAccountCarModelMapper divisionAccountCarModelMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DivisionAccountCarModel queryByIdFromDB(Long id) {
        return this.divisionAccountCarModelMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DivisionAccountCarModel queryByIdFromCache(Long id) {
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
    public List<DivisionAccountCarModel> queryAllByLimit(int offset, int limit) {
        return this.divisionAccountCarModelMapper.queryAllByLimit(offset, limit);
    }

    @Slave
    @Override
    public List<Long> selectByDivisionAccountConfigId(Long id) {
        return this.divisionAccountCarModelMapper.selectByDivisionAccountConfigId(id);
    }

    @Slave
    @Override
    public Long selectByCarModelId(Long carModelId) {
        return this.divisionAccountCarModelMapper.selectByCarModelId(carModelId);
    }

    /**
     * 新增数据
     *
     * @param divisionAccountCarModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivisionAccountCarModel insert(DivisionAccountCarModel divisionAccountCarModel) {
        this.divisionAccountCarModelMapper.insertOne(divisionAccountCarModel);
        return divisionAccountCarModel;
    }

    @Override
    public Integer batchInsert(List<DivisionAccountCarModel> divisionAccountCarModelList) {
        return this.divisionAccountCarModelMapper.batchInsert(divisionAccountCarModelList);
    }

    /**
     * 修改数据
     *
     * @param divisionAccountCarModel 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DivisionAccountCarModel divisionAccountCarModel) {
        return this.divisionAccountCarModelMapper.update(divisionAccountCarModel);

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
        return this.divisionAccountCarModelMapper.deleteById(id) > 0;
    }
}
