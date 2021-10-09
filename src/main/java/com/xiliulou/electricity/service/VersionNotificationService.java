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
     * 修改数据
     *
     * @param versionNotification 实例对象
     * @return 实例对象
     */
    Integer update(VersionNotification versionNotification);


    Triple<Boolean, String, Object> updateNotification(VersionNotificationQuery versionNotificationQuery);

}
