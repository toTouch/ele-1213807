package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.CabinetMoveHistory;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (CabinetMoveHistory)表数据库访问层
 *
 * @author zzlong
 * @since 2023-06-15 19:54:29
 */
public interface CabinetMoveHistoryMapper extends BaseMapper<CabinetMoveHistory> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CabinetMoveHistory queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<CabinetMoveHistory> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param cabinetMoveHistory 实例对象
     * @return 对象列表
     */
    List<CabinetMoveHistory> queryAll(CabinetMoveHistory cabinetMoveHistory);

    /**
     * 新增数据
     *
     * @param cabinetMoveHistory 实例对象
     * @return 影响行数
     */
    int insertOne(CabinetMoveHistory cabinetMoveHistory);

    /**
     * 修改数据
     *
     * @param cabinetMoveHistory 实例对象
     * @return 影响行数
     */
    int update(CabinetMoveHistory cabinetMoveHistory);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
