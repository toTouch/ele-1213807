package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderOperHistoryMapper;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@Service("electricityCabinetOrderOperHistoryService")
public class ElectricityCabinetOrderOperHistoryServiceImpl implements ElectricityCabinetOrderOperHistoryService {
    @Resource
    private ElectricityCabinetOrderOperHistoryMapper electricityCabinetOrderOperHistoryMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrderOperHistory queryByIdFromDB(Long id) {
        return this.electricityCabinetOrderOperHistoryMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrderOperHistory queryByIdFromCache(Long id) {
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
    public List<ElectricityCabinetOrderOperHistory> queryAllByLimit(int offset, int limit) {
        return this.electricityCabinetOrderOperHistoryMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetOrderOperHistory insert(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory) {
        this.electricityCabinetOrderOperHistoryMapper.insert(electricityCabinetOrderOperHistory);
        return electricityCabinetOrderOperHistory;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory) {
       return this.electricityCabinetOrderOperHistoryMapper.update(electricityCabinetOrderOperHistory);
         
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
        return this.electricityCabinetOrderOperHistoryMapper.deleteById(id) > 0;
    }
}