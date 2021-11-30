package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ApiReturnOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ApiReturnOrder)表数据库访问层
 *
 * @author makejava
 * @since 2021-11-10 10:15:27
 */
public interface ApiReturnOrderMapper  extends BaseMapper<ApiReturnOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ApiReturnOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<ApiReturnOrder> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param apiReturnOrder 实例对象
     * @return 对象列表
     */
    List<ApiReturnOrder> queryAll(ApiReturnOrder apiReturnOrder);

    /**
     * 新增数据
     *
     * @param apiReturnOrder 实例对象
     * @return 影响行数
     */
    int insertOne(ApiReturnOrder apiReturnOrder);

    /**
     * 修改数据
     *
     * @param apiReturnOrder 实例对象
     * @return 影响行数
     */
    int update(ApiReturnOrder apiReturnOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    ApiReturnOrder queryByOrderId(@Param("orderId") String orderId, @Param("tenantId") Integer tenantId);
}
