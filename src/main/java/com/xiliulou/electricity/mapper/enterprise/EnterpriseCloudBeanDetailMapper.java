package com.xiliulou.electricity.mapper.enterprise;



import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企业云豆详情表(TEnterpriseCloudBeanDetail)表数据库访问层
 *
 * @author maxiaodong
 * @since 2025-01-15 19:18:06
 */
public interface EnterpriseCloudBeanDetailMapper {

    EnterpriseCloudBeanDetail selectByEnterpriseId(@Param("enterpriseId") Long id);
}

