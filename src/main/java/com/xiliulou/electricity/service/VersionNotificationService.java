package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.VersionNotification;
import com.xiliulou.electricity.query.VersionNotificationQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (VersionNotification)表服务接口
 *
 * @author makejava
 * @since 2021-09-26 14:36:06
 */
public interface VersionNotificationService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    VersionNotification queryByIdFromDB(Integer id);

      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    VersionNotification queryByIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<VersionNotification> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param versionNotification 实例对象
     * @return 实例对象
     */
    VersionNotification insert(VersionNotification versionNotification);

    /**
     * 修改数据
     *
     * @param versionNotification 实例对象
     * @return 实例对象
     */
    Integer update(VersionNotification versionNotification);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

    Triple<Boolean, String, Object> updateNotification(VersionNotificationQuery versionNotificationQuery);

}
