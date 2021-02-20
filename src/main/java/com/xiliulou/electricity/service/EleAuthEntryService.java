package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.EleAuthEntry;

/**
 * 实名认证资料项(TEleAuthEntry)表服务接口
 *
 * @author makejava
 * @since 2021-02-20 13:37:11
 */
public interface EleAuthEntryService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleAuthEntry queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleAuthEntry queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param eleAuthEntry 实例对象
     * @return 实例对象
     */
    EleAuthEntry insert(EleAuthEntry eleAuthEntry);

    /**
     * 修改数据
     *
     * @param eleAuthEntry 实例对象
     * @return 实例对象
     */
    Integer update(EleAuthEntry eleAuthEntry);


}