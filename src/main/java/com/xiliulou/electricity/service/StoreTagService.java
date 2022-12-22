package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.StoreTag;
import com.xiliulou.electricity.query.StoreTagQuery;

import java.util.List;

/**
 * 门店标签表(StoreTag)表服务接口
 *
 * @author zzlong
 * @since 2022-12-14 13:55:07
 */
public interface StoreTagService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreTag selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreTag selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<StoreTag> selectByPage(StoreTagQuery storeTagQuery);


    Integer selectPageCount(StoreTagQuery query);

    /**
     * 新增数据
     *
     * @return 实例对象
     */
    Integer insert(StoreTagQuery storeTagQuery);

    Integer batchInsert(List<StoreTag> storeTags);

    /**
     * 修改数据
     *
     * @return 实例对象
     */
    Integer update(StoreTagQuery storeTagQuery);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    int deleteByStoreId(Long id);

    List<StoreTag> selectByStoreId(Long id);
}
