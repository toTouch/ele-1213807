package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityAppConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 用户列表(ElectricityAppConfig)表数据库访问层
 *
 * @author zhangyongbo
 * @since 2023-10-11
 */
public interface ElectricityAppConfigMapper extends BaseMapper<ElectricityAppConfig>{

    Integer update(ElectricityAppConfig electricityAppConfig);
    
    Integer deleteByUid(@Param("uid") Long uid);
    
    Integer deleteByTenantId(@Param("tenantId") Integer tenantId);
    
    ElectricityAppConfig selectElectricityAppConfig(@Param("uid") Long uid,@Param("tenantId") Integer tenantId);

}