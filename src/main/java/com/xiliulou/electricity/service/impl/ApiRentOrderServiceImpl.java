package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ApiRentOrder;
import com.xiliulou.electricity.mapper.ApiRentOrderMapper;
import com.xiliulou.electricity.service.ApiRentOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * (ApiRentOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-11-09 13:32:28
 */
@Service("apiRentOrderService")
@Slf4j
public class ApiRentOrderServiceImpl implements ApiRentOrderService {
    @Resource
    private ApiRentOrderMapper apiRentOrderMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiRentOrder queryByIdFromDB(Long id) {
        return this.apiRentOrderMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ApiRentOrder queryByIdFromCache(Long id) {
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
    public List<ApiRentOrder> queryAllByLimit(int offset, int limit) {
        return this.apiRentOrderMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param apiRentOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiRentOrder insert(ApiRentOrder apiRentOrder) {
        this.apiRentOrderMapper.insertOne(apiRentOrder);
        return apiRentOrder;
    }

    /**
     * 修改数据
     *
     * @param apiRentOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ApiRentOrder apiRentOrder) {
        return this.apiRentOrderMapper.update(apiRentOrder);

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
        return this.apiRentOrderMapper.deleteById(id) > 0;
    }

    @Override
    public ApiRentOrder queryByOrderId(String orderId, Integer tenantId) {
        return this.apiRentOrderMapper.queryByOrderId(orderId, tenantId);
    }
}
