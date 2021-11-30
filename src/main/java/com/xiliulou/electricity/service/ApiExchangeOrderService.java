package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ApiExchangeOrder;
import java.util.List;

/**
 * (ApiExchangeOrder)表服务接口
 *
 * @author makejava
 * @since 2021-11-10 14:10:08
 */
public interface ApiExchangeOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiExchangeOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiExchangeOrder queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ApiExchangeOrder> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param apiExchangeOrder 实例对象
     * @return 实例对象
     */
    ApiExchangeOrder insert(ApiExchangeOrder apiExchangeOrder);

    /**
     * 修改数据
     *
     * @param apiExchangeOrder 实例对象
     * @return 实例对象
     */
    Integer update(ApiExchangeOrder apiExchangeOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    ApiExchangeOrder queryByOrderId(String orderId, Integer tenantId);
}
