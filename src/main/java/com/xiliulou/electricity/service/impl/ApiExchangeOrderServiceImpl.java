package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ApiExchangeOrder;
import com.xiliulou.electricity.mapper.ApiExchangeOrderMapper;
import com.xiliulou.electricity.service.ApiExchangeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (ApiExchangeOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-11-10 14:10:08
 */
@Service("apiExchangeOrderService")
@Slf4j
public class ApiExchangeOrderServiceImpl implements ApiExchangeOrderService {
    @Resource
    private ApiExchangeOrderMapper apiExchangeOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiExchangeOrder queryByIdFromDB(Long id) {
        return this.apiExchangeOrderMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiExchangeOrder queryByIdFromCache(Long id) {
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
    public List<ApiExchangeOrder> queryAllByLimit(int offset, int limit) {
        return this.apiExchangeOrderMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param apiExchangeOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiExchangeOrder insert(ApiExchangeOrder apiExchangeOrder) {
        this.apiExchangeOrderMapper.insertOne(apiExchangeOrder);
        return apiExchangeOrder;
    }

    /**
     * 修改数据
     *
     * @param apiExchangeOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ApiExchangeOrder apiExchangeOrder) {
        return this.apiExchangeOrderMapper.update(apiExchangeOrder);

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
        return this.apiExchangeOrderMapper.deleteById(id) > 0;
    }

    @Override
    public ApiExchangeOrder queryByOrderId(String orderId, Integer tenantId) {
        return this.apiExchangeOrderMapper.queryByOrderId(orderId, tenantId);
    }
}
