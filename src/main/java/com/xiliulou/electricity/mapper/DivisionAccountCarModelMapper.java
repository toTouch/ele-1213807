package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.DivisionAccountCarModel;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (DivisionAccountCarModel)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-23 18:00:15
 */
public interface DivisionAccountCarModelMapper extends BaseMapper<DivisionAccountCarModel> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountCarModel queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<DivisionAccountCarModel> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param divisionAccountCarModel 实例对象
     * @return 对象列表
     */
    List<DivisionAccountCarModel> queryAll(DivisionAccountCarModel divisionAccountCarModel);

    /**
     * 新增数据
     *
     * @param divisionAccountCarModel 实例对象
     * @return 影响行数
     */
    int insertOne(DivisionAccountCarModel divisionAccountCarModel);

    /**
     * 修改数据
     *
     * @param divisionAccountCarModel 实例对象
     * @return 影响行数
     */
    int update(DivisionAccountCarModel divisionAccountCarModel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer batchInsert(List<DivisionAccountCarModel> divisionAccountCarModelList);

    List<Long> selectByDivisionAccountConfigId(@Param("id") Long id);

    Long selectByCarModelId(@Param("carModelId") Long carModelId);

    List<Long> selectByTenantId(@Param("tenantId") Integer tenantId);

    Integer deleteByDivisionAccountId(@Param("divisionAccountId") Long divisionAccountId);
}
