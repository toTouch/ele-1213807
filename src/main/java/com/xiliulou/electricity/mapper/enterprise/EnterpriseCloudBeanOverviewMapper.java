package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOverview;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业云豆总览表(TEnterpriseCloudBeanOverview)表数据库访问层
 *
 * @author makejava
 * @since 2025-01-15 19:56:24
 */
public interface EnterpriseCloudBeanOverviewMapper {

    EnterpriseCloudBeanOverview selectByEnterpriseId(@Param("id") Long id);
}

