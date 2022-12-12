package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Region;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (Region)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-12 11:38:20
 */
public interface RegionMapper extends BaseMapper<Region> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Region selectById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<Region> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param region 实例对象
     * @return 对象列表
     */
    List<Region> selectByQuery(Region region);

    /**
     * 新增数据
     *
     * @param region 实例对象
     * @return 影响行数
     */
    int insertOne(Region region);

    /**
     * 修改数据
     *
     * @param region 实例对象
     * @return 影响行数
     */
    int update(Region region);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    /**
     * 通过code查询单条数据
     *
     * @param code 主键
     * @return 实例对象
     */
    Region selectByCode(String code);
}
