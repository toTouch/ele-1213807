package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CarRefundOrder;
import com.xiliulou.electricity.query.CarRefundOrderQuery;

import java.util.List;

/**
 * (CarRefundOrder)表服务接口
 *
 * @author Hardy
 * @since 2023-03-15 13:41:59
 */
public interface CarRefundOrderService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    CarRefundOrder queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    CarRefundOrder queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<CarRefundOrder> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param carRefundOrder 实例对象
     * @return 实例对象
     */
    CarRefundOrder insert(CarRefundOrder carRefundOrder);
    
    /**
     * 修改数据
     *
     * @param carRefundOrder 实例对象
     * @return 实例对象
     */
    Integer update(CarRefundOrder carRefundOrder);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    R userCarRefundOrder();
    
    R queryList(CarRefundOrderQuery query);
    
    Integer queryCountByStatus(Long uid, Integer tenantId, Integer status);
    
    R queryCount(CarRefundOrderQuery query);
}
