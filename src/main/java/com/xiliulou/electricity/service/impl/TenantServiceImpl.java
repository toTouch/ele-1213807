package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.Tenant;
import com.xiliulou.electricity.mapper.TenantMapper;
import com.xiliulou.electricity.service.TenantService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * 租户表(Tenant)表服务实现类
 *
 * @author makejava
 * @since 2021-06-16 14:31:45
 */
@Service("tenantService")
@Slf4j
public class TenantServiceImpl implements TenantService {
    @Resource
    private TenantMapper tenantMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Tenant queryByIdFromDB(Integer id) {
        return this.tenantMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  Tenant queryByIdFromCache(Integer id) {
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
    public List<Tenant> queryAllByLimit(int offset, int limit) {
        return this.tenantMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param tenant 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant insert(Tenant tenant) {
        this.tenantMapper.insertOne(tenant);
        return tenant;
    }

    /**
     * 修改数据
     *
     * @param tenant 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(Tenant tenant) {
       return this.tenantMapper.update(tenant);
         
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
        return this.tenantMapper.deleteById(id) > 0;
    }
}