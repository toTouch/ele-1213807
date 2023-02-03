package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ThirdConfig;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ThirdConfig)表数据库访问层
 *
 * @author zzlong
 * @since 2023-02-02 18:03:58
 */
public interface ThirdConfigMapper extends BaseMapper<ThirdConfig> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdConfig selectById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ThirdConfig> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param thirdConfig 实例对象
     * @return 对象列表
     */
    List<ThirdConfig> selectByQuery(ThirdConfig thirdConfig);

    /**
     * 新增数据
     *
     * @param thirdConfig 实例对象
     * @return 影响行数
     */
    int insertOne(ThirdConfig thirdConfig);

    /**
     * 修改数据
     *
     * @param thirdConfig 实例对象
     * @return 影响行数
     */
    int update(ThirdConfig thirdConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    ThirdConfig selectLatestByTenantId(@Param("tenantId") Integer tenantId);
}
