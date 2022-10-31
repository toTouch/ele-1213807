package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.EleCabinetCoreData;
import com.xiliulou.electricity.mapper.EleCabinetCoreDataMapper;
import com.xiliulou.electricity.query.EleCabinetCoreDataQuery;
import com.xiliulou.electricity.service.EleCabinetCoreDataService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 柜机核心板上报数据(EleCabinetCoreData)表服务实现类
 *
 * @author zzlong
 * @since 2022-07-06 14:20:37
 */
@Service("eleCabinetCoreDataService")
@Slf4j
public class EleCabinetCoreDataServiceImpl implements EleCabinetCoreDataService {
    @Resource
    private EleCabinetCoreDataMapper eleCabinetCoreDataMapper;

    @Override
    public int idempotentUpdateCabinetCoreData(EleCabinetCoreData cabinetCoreData) {
        return eleCabinetCoreDataMapper.idempotentUpdateCabinetCoreData(cabinetCoreData);
    }

    @Override
    public List<EleCabinetCoreData> selectListByQuery(EleCabinetCoreDataQuery eleCabinetCoreDataQuery) {
        return eleCabinetCoreDataMapper.selectListByQuery(eleCabinetCoreDataQuery);
    }

    @Override
    public EleCabinetCoreData selectByEleCabinetId(Integer id) {
        return eleCabinetCoreDataMapper.selectOne(new LambdaQueryWrapper<EleCabinetCoreData>().eq(EleCabinetCoreData::getElectricityCabinetId,id));
    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleCabinetCoreData queryByIdFromDB(Long id) {
        return this.eleCabinetCoreDataMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleCabinetCoreData queryByIdFromCache(Long id) {
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
    public List<EleCabinetCoreData> queryAllByLimit(int offset, int limit) {
        return this.eleCabinetCoreDataMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param eleCabinetCoreData 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleCabinetCoreData insert(EleCabinetCoreData eleCabinetCoreData) {
        this.eleCabinetCoreDataMapper.insertOne(eleCabinetCoreData);
        return eleCabinetCoreData;
    }

    /**
     * 修改数据
     *
     * @param eleCabinetCoreData 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleCabinetCoreData eleCabinetCoreData) {
        return this.eleCabinetCoreDataMapper.update(eleCabinetCoreData);

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
        return this.eleCabinetCoreDataMapper.deleteById(id) > 0;
    }
}
