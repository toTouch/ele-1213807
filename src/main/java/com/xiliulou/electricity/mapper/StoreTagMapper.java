package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.StoreTag;

import java.util.List;

import com.xiliulou.electricity.query.StoreTagQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 门店标签表(StoreTag)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-14 13:55:07
 */
public interface StoreTagMapper extends BaseMapper<StoreTag> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreTag selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<StoreTag> selectByPage(StoreTagQuery storeTagQuery);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param storeTag 实例对象
     * @return 对象列表
     */
    List<StoreTag> selectByQuery(StoreTag storeTag);

    /**
     * 新增数据
     *
     * @param storeTag 实例对象
     * @return 影响行数
     */
    int insertOne(StoreTag storeTag);

    int batchInsert(List<StoreTag> storeTags);

    /**
     * 修改数据
     *
     * @param storeTag 实例对象
     * @return 影响行数
     */
    int update(StoreTag storeTag);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectPageCount(StoreTagQuery query);

    int deleteByStoreId(@Param("storeId") Long storeId);
}
