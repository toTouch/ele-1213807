package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ShareActivityOperateRecord;

import java.util.List;

import com.xiliulou.electricity.query.ShareActivityQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (ShareActivityOperateRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-05-24 14:47:17
 */
public interface ShareActivityOperateRecordMapper extends BaseMapper<ShareActivityOperateRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ShareActivityOperateRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ShareActivityOperateRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 对象列表
     */
    List<ShareActivityOperateRecord> queryAll(ShareActivityOperateRecord shareActivityOperateRecord);

    /**
     * 新增数据
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ShareActivityOperateRecord shareActivityOperateRecord);

    /**
     * 修改数据
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 影响行数
     */
    int update(ShareActivityOperateRecord shareActivityOperateRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectByPageCount(ShareActivityQuery query);

    List<ShareActivityOperateRecord> selectByPage(ShareActivityQuery query);
}
