package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FranchiseeMoveRecord;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 加盟商迁移记录(FranchiseeMoveRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-02-07 17:12:51
 */
public interface FranchiseeMoveRecordMapper extends BaseMapper<FranchiseeMoveRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FranchiseeMoveRecord selectById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FranchiseeMoveRecord> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 对象列表
     */
    List<FranchiseeMoveRecord> selectByQuery(FranchiseeMoveRecord franchiseeMoveRecord);

    /**
     * 新增数据
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 影响行数
     */
    int insertOne(FranchiseeMoveRecord franchiseeMoveRecord);

    /**
     * 修改数据
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 影响行数
     */
    int update(FranchiseeMoveRecord franchiseeMoveRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}
