package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.TenantCallingApiStats;
import com.xiliulou.electricity.mapper.TenantCallingApiStatsMapper;
import com.xiliulou.electricity.service.TenantCallingApiStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 10:21
 * @Description:
 */
@Service("tenantCallingApiStatsService")
@Slf4j
public class TenantCallingApiStatsServiceImpl implements TenantCallingApiStatsService {
    @Resource
    private TenantCallingApiStatsMapper tenantCallingApiStatsMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public TenantCallingApiStats queryByTenantIdFromDB(Integer id) {
        return this.tenantCallingApiStatsMapper.queryByTenantId(id);
    }

        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  TenantCallingApiStats queryByIdFromCache(Integer id) {
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
    public List<TenantCallingApiStats> queryAllByLimit(int offset, int limit) {
        return this.tenantCallingApiStatsMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param tenantCallingApiStats 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCallingApiStats insert(TenantCallingApiStats tenantCallingApiStats) {
        this.tenantCallingApiStatsMapper.insertOne(tenantCallingApiStats);
        return tenantCallingApiStats;
    }

    /**
     * 修改数据
     *
     * @param tenantCallingApiStats 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(TenantCallingApiStats tenantCallingApiStats) {
       return this.tenantCallingApiStatsMapper.update(tenantCallingApiStats);

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
        return this.tenantCallingApiStatsMapper.deleteById(id) > 0;
    }
}
