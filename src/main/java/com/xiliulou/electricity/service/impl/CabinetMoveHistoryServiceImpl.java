package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.CabinetMoveHistory;
import com.xiliulou.electricity.mapper.CabinetMoveHistoryMapper;
import com.xiliulou.electricity.service.CabinetMoveHistoryService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (CabinetMoveHistory)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-15 19:54:29
 */
@Service("cabinetMoveHistoryService")
@Slf4j
public class CabinetMoveHistoryServiceImpl implements CabinetMoveHistoryService {
    @Resource
    private CabinetMoveHistoryMapper cabinetMoveHistoryMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CabinetMoveHistory queryByIdFromDB(Long id) {
        return this.cabinetMoveHistoryMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CabinetMoveHistory queryByIdFromCache(Long id) {
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
    public List<CabinetMoveHistory> queryAllByLimit(int offset, int limit) {
        return this.cabinetMoveHistoryMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param cabinetMoveHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CabinetMoveHistory insert(CabinetMoveHistory cabinetMoveHistory) {
        this.cabinetMoveHistoryMapper.insertOne(cabinetMoveHistory);
        return cabinetMoveHistory;
    }

    /**
     * 修改数据
     *
     * @param cabinetMoveHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CabinetMoveHistory cabinetMoveHistory) {
        return this.cabinetMoveHistoryMapper.update(cabinetMoveHistory);

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
        return this.cabinetMoveHistoryMapper.deleteById(id) > 0;
    }
}
