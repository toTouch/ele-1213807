package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FreeDepositOrder;
import com.xiliulou.electricity.query.FreeDepositQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (FreeDepositOrder)表服务接口
 *
 * @author makejava
 * @since 2023-02-15 11:39:27
 */
public interface FreeDepositOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositOrder queryByIdFromDB(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<FreeDepositOrder> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    FreeDepositOrder insert(FreeDepositOrder freeDepositOrder);

    /**
     * 修改数据
     *
     * @param freeDepositOrder 实例对象
     * @return 实例对象
     */
    Integer update(FreeDepositOrder freeDepositOrder);

    
    Triple<Boolean, String, Object> freeDepositOrder(FreeDepositQuery freeDepositQuery);
    
    Triple<Boolean, String, Object> freeDepositPreCheck();
    
}
