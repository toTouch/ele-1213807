package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.Picture;

import java.util.List;

import com.xiliulou.electricity.query.PictureQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 图片表(Picture)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-14 13:54:08
 */
public interface PictureMapper extends BaseMapper<Picture> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Picture selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<Picture> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param pictureQuery 实例对象
     * @return 对象列表
     */
    List<Picture> selectByQuery(PictureQuery pictureQuery);

    /**
     * 新增数据
     *
     * @param picture 实例对象
     * @return 影响行数
     */
    int insertOne(Picture picture);

    Integer batchInsert(List<Picture> pictures);
    /**
     * 修改数据
     *
     * @param picture 实例对象
     * @return 影响行数
     */
    int update(Picture picture);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int deleteByBusinessId(@Param("businessId") Long businessId);
}
