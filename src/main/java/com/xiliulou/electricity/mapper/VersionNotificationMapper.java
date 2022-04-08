package com.xiliulou.electricity.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.VersionNotification;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (VersionNotification)表数据库访问层
 *
 * @author makejava
 * @since 2021-09-26 14:36:05
 */
public interface VersionNotificationMapper extends BaseMapper<VersionNotification> {

    List<VersionNotification> queryVersionPage(@Param("offset")Long offset,Long size);

    VersionNotification queryCreateTimeMaxTenantNotification();

}
