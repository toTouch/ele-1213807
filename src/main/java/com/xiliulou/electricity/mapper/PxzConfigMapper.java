package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.PxzConfig;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (PxzConfig)表数据库访问层
 *
 * @author makejava
 * @since 2023-02-15 16:23:54
 */
public interface PxzConfigMapper  extends BaseMapper<PxzConfig>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    PxzConfig queryByTenantId(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<PxzConfig> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param pxzConfig 实例对象
     * @return 对象列表
     */
    List<PxzConfig> queryAll(PxzConfig pxzConfig);

    /**
     * 新增数据
     *
     * @param pxzConfig 实例对象
     * @return 影响行数
     */
    int insertOne(PxzConfig pxzConfig);

    /**
     * 修改数据
     *
     * @param pxzConfig 实例对象
     * @return 影响行数
     */
    int update(PxzConfig pxzConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
