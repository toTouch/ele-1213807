package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.StoreDetail;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 门店详情表(StoreDetail)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-14 13:54:40
 */
public interface StoreDetailMapper extends BaseMapper<StoreDetail> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreDetail selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<StoreDetail> selectByPage(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param storeDetail 实例对象
     * @return 对象列表
     */
    List<StoreDetail> selectByQuery(StoreDetail storeDetail);

    /**
     * 新增数据
     *
     * @param storeDetail 实例对象
     * @return 影响行数
     */
    int insertOne(StoreDetail storeDetail);

    /**
     * 修改数据
     *
     * @param storeDetail 实例对象
     * @return 影响行数
     */
    int update(StoreDetail storeDetail);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    int deleteByStoreId(@Param("storeId") Long storeId);
}
