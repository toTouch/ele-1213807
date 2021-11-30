package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ApiReturnOrder;
import java.util.List;

/**
 * (ApiReturnOrder)表服务接口
 *
 * @author makejava
 * @since 2021-11-10 10:15:27
 */
public interface ApiReturnOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiReturnOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiReturnOrder queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ApiReturnOrder> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param apiReturnOrder 实例对象
     * @return 实例对象
     */
    ApiReturnOrder insert(ApiReturnOrder apiReturnOrder);

    /**
     * 修改数据
     *
     * @param apiReturnOrder 实例对象
     * @return 实例对象
     */
    Integer update(ApiReturnOrder apiReturnOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    ApiReturnOrder queryByOrderId(String orderId, Integer tenantId);
}
