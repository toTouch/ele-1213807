package com.xiliulou.electricity.mapper;

import java.util.List;

import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.query.FreeDepositAlipayHistoryQuery;
import com.xiliulou.electricity.vo.FreeDepositAlipayHistoryVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FreeDepositAlipayHistory)表数据库访问层
 *
 * @author zgw
 * @since 2023-04-13 09:13:00
 */
public interface FreeDepositAlipayHistoryMapper extends BaseMapper<FreeDepositAlipayHistory> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositAlipayHistory queryById(Long id);
    
    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FreeDepositAlipayHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);
    
    
    /**
     * 通过实体作为筛选条件查询
     *
     * @param freeDepositAlipayHistory 实例对象
     * @return 对象列表
     */
    List<FreeDepositAlipayHistory> queryAll(FreeDepositAlipayHistory freeDepositAlipayHistory);
    
    /**
     * 修改数据
     *
     * @param freeDepositAlipayHistory 实例对象
     * @return 影响行数
     */
    int update(FreeDepositAlipayHistory freeDepositAlipayHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<FreeDepositAlipayHistoryVo> queryList(FreeDepositAlipayHistoryQuery query);
    
    Long queryCount(FreeDepositAlipayHistoryQuery query);
    
    FreeDepositAlipayHistory queryByOrderId(@Param("orderId") String orderId);
    
    Integer updateByOrderId(FreeDepositAlipayHistory freeDepositAlipayHistory);
}
