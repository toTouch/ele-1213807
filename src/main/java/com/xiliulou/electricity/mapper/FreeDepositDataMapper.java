package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.FreeDepositData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (FreeDepositData)表数据库访问层
 *
 * @author zzlong
 * @since 2023-02-20 15:46:34
 */
public interface FreeDepositDataMapper extends BaseMapper<FreeDepositData> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositData selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FreeDepositData> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param freeDepositData 实例对象
     * @return 对象列表
     */
    List<FreeDepositData> selectByQuery(FreeDepositData freeDepositData);

    /**
     * 新增数据
     *
     * @param freeDepositData 实例对象
     * @return 影响行数
     */
    int insertOne(FreeDepositData freeDepositData);

    /**
     * 修改数据
     *
     * @param freeDepositData 实例对象
     * @return 影响行数
     */
    int update(FreeDepositData freeDepositData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    FreeDepositData selectByTenantId(@Param("tenantId") Integer tenantId);

    Integer deductionFreeDepositCapacity(@Param("tenantId") Integer tenantId, @Param("count") Integer count);
}
