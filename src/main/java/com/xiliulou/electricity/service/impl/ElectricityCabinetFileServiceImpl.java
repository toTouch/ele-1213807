package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityCabinetFile;
import com.xiliulou.electricity.mapper.ElectricityCabinetFileMapper;
import com.xiliulou.electricity.service.ElectricityCabinetFileService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜文件表(TElectricityCabinetFile)表服务实现类
 *
 * @author makejava
 * @since 2020-11-27 10:17:18
 */
@Service("tElectricityCabinetFileService")
public class ElectricityCabinetFileServiceImpl implements ElectricityCabinetFileService {
    @Resource
    private ElectricityCabinetFileMapper electricityCabinetFileMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetFile queryByIdFromDB(Long id) {
        return this.electricityCabinetFileMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetFile queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetFile> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetFileMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetFile insert(ElectricityCabinetFile electricityCabinetFile) {
        this.electricityCabinetFileMapper.insertOne(electricityCabinetFile);
        return electricityCabinetFile;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetFile 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetFile electricityCabinetFile) {
       return this.electricityCabinetFileMapper.update(electricityCabinetFile);
         
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
        return this.electricityCabinetFileMapper.deleteById(id) > 0;
    }

    @Override
    public List<ElectricityCabinetFile> queryByDeviceInfo(Long electricityCabinetId, Integer fileType) {
        return null;
    }
}