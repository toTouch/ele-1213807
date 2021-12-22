package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ApiReturnOrder;
import com.xiliulou.electricity.mapper.ApiReturnOrderMapper;
import com.xiliulou.electricity.service.ApiReturnOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (ApiReturnOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-11-10 10:15:27
 */
@Service("apiReturnOrderService")
@Slf4j
public class ApiReturnOrderServiceImpl implements ApiReturnOrderService {
    @Resource
    private ApiReturnOrderMapper apiReturnOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiReturnOrder queryByIdFromDB(Long id) {
        return this.apiReturnOrderMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiReturnOrder queryByIdFromCache(Long id) {
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
    public List<ApiReturnOrder> queryAllByLimit(int offset, int limit) {
        return this.apiReturnOrderMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param apiReturnOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiReturnOrder insert(ApiReturnOrder apiReturnOrder) {
        this.apiReturnOrderMapper.insertOne(apiReturnOrder);
        return apiReturnOrder;
    }

    /**
     * 修改数据
     *
     * @param apiReturnOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ApiReturnOrder apiReturnOrder) {
        return this.apiReturnOrderMapper.update(apiReturnOrder);

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
        return this.apiReturnOrderMapper.deleteById(id) > 0;
    }

    @Override
    public ApiReturnOrder queryByOrderId(String orderId, Integer tenantId) {
        return this.apiReturnOrderMapper.queryByOrderId(orderId, tenantId);
    }
}
