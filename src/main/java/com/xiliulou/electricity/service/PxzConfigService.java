package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.PxzConfig;
import com.xiliulou.electricity.query.PxzConfigQuery;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * (PxzConfig)表服务接口
 *
 * @author makejava
 * @since 2023-02-15 16:23:54
 */
public interface PxzConfigService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    PxzConfig queryByTenantIdFromDB(Integer id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    PxzConfig queryByTenantIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<PxzConfig> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param pxzConfig 实例对象
     * @return 实例对象
     */
    PxzConfig insert(PxzConfig pxzConfig);

    /**
     * 修改数据
     *
     * @param pxzConfig 实例对象
     * @return 实例对象
     */
    Integer update(PxzConfig pxzConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    Pair<Boolean, Object> queryByInfo();
    
    Pair<Boolean, Object> save(PxzConfigQuery pxzConfigQuery);
    
    Pair<Boolean, Object> modify(PxzConfigQuery pxzConfigQuery);

    Pair<Boolean, Object> check();
}
