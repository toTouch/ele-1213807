package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.DivisionAccountConfig;

import java.util.List;

import com.xiliulou.electricity.query.DivisionAccountConfigQuery;
import com.xiliulou.electricity.vo.DivisionAccountConfigVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (DivisionAccountConfig)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-23 18:00:37
 */
public interface DivisionAccountConfigMapper extends BaseMapper<DivisionAccountConfig> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountConfig queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<DivisionAccountConfig> selectByPage(DivisionAccountConfigQuery query);

    Integer selectByPageCount(DivisionAccountConfigQuery query);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param divisionAccountConfig 实例对象
     * @return 对象列表
     */
    List<DivisionAccountConfig> queryAll(DivisionAccountConfig divisionAccountConfig);

    /**
     * 新增数据
     *
     * @param divisionAccountConfig 实例对象
     * @return 影响行数
     */
    int insertOne(DivisionAccountConfig divisionAccountConfig);

    /**
     * 修改数据
     *
     * @param divisionAccountConfig 实例对象
     * @return 影响行数
     */
    int update(DivisionAccountConfig divisionAccountConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);




}
