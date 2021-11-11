package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ApiOrderOperHistory;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ApiOrderOperHistory)表数据库访问层
 *
 * @author makejava
 * @since 2021-11-09 16:57:54
 */
public interface ApiOrderOperHistoryMapper  extends BaseMapper<ApiOrderOperHistory>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiOrderOperHistory queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ApiOrderOperHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param apiOrderOperHistory 实例对象
     * @return 对象列表
     */
    List<ApiOrderOperHistory> queryAll(ApiOrderOperHistory apiOrderOperHistory);

    /**
     * 新增数据
     *
     * @param apiOrderOperHistory 实例对象
     * @return 影响行数
     */
    int insertOne(ApiOrderOperHistory apiOrderOperHistory);

    /**
     * 修改数据
     *
     * @param apiOrderOperHistory 实例对象
     * @return 影响行数
     */
    int update(ApiOrderOperHistory apiOrderOperHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ApiOrderOperHistory> queryByOrderId(@Param("orderId") String orderId, @Param("type") Integer orderTypeExchange);
}
