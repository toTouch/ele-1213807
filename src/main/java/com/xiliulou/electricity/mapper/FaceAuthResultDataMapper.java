package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FaceAuthResultData;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FaceAuthResultData)表数据库访问层
 *
 * @author zzlong
 * @since 2023-02-03 11:03:24
 */
public interface FaceAuthResultDataMapper extends BaseMapper<FaceAuthResultData> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FaceAuthResultData selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<FaceAuthResultData> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param faceAuthResultData 实例对象
     * @return 对象列表
     */
    List<FaceAuthResultData> selectByQuery(FaceAuthResultData faceAuthResultData);

    /**
     * 新增数据
     *
     * @param faceAuthResultData 实例对象
     * @return 影响行数
     */
    int insertOne(FaceAuthResultData faceAuthResultData);

    /**
     * 修改数据
     *
     * @param faceAuthResultData 实例对象
     * @return 影响行数
     */
    int update(FaceAuthResultData faceAuthResultData);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}
