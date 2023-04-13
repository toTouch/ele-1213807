package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.query.FreeDepositAlipayHistoryQuery;

import java.util.List;

/**
 * (FreeDepositAlipayHistory)表服务接口
 *
 * @author zgw
 * @since 2023-04-13 09:13:01
 */
public interface FreeDepositAlipayHistoryService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositAlipayHistory queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositAlipayHistory queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FreeDepositAlipayHistory> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param freeDepositAlipayHistory 实例对象
     * @return 实例对象
     */
    FreeDepositAlipayHistory insert(FreeDepositAlipayHistory freeDepositAlipayHistory);
    
    /**
     * 修改数据
     *
     * @param freeDepositAlipayHistory 实例对象
     * @return 实例对象
     */
    Integer update(FreeDepositAlipayHistory freeDepositAlipayHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    R queryList(FreeDepositAlipayHistoryQuery query);
    
    R queryCount(FreeDepositAlipayHistoryQuery query);
}
