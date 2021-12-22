package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ApiRentOrder;
import java.util.List;

/**
 * (ApiRentOrder)表服务接口
 *
 * @author makejava
 * @since 2021-11-09 13:32:28
 */
public interface ApiRentOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiRentOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiRentOrder queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ApiRentOrder> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param apiRentOrder 实例对象
     * @return 实例对象
     */
    ApiRentOrder insert(ApiRentOrder apiRentOrder);

    /**
     * 修改数据
     *
     * @param apiRentOrder 实例对象
     * @return 实例对象
     */
    Integer update(ApiRentOrder apiRentOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    ApiRentOrder queryByOrderId(String orderId, Integer tenantId);
}
