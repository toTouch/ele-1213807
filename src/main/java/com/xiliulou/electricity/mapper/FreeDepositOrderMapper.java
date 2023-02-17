package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FreeDepositOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FreeDepositOrder)表数据库访问层
 *
 * @author makejava
 * @since 2023-02-15 11:39:27
 */
public interface FreeDepositOrderMapper  extends BaseMapper<FreeDepositOrder>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositOrder queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<FreeDepositOrder> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param freeDepositOrder 实例对象
     * @return 对象列表
     */
    List<FreeDepositOrder> queryAll(FreeDepositOrder freeDepositOrder);

    /**
     * 新增数据
     *
     * @param freeDepositOrder 实例对象
     * @return 影响行数
     */
    int insertOne(FreeDepositOrder freeDepositOrder);

    /**
     * 修改数据
     *
     * @param freeDepositOrder 实例对象
     * @return 影响行数
     */
    int update(FreeDepositOrder freeDepositOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
