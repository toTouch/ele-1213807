package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.StoreDetail;

import java.util.List;

/**
 * 门店详情表(StoreDetail)表服务接口
 *
 * @author zzlong
 * @since 2022-12-14 13:54:40
 */
public interface StoreDetailService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreDetail selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    StoreDetail selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<StoreDetail> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param storeDetail 实例对象
     * @return 实例对象
     */
    StoreDetail insert(StoreDetail storeDetail);

    /**
     * 修改数据
     *
     * @param storeDetail 实例对象
     * @return 实例对象
     */
    Integer update(StoreDetail storeDetail);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    int deleteByStoreId(Long id);
}
