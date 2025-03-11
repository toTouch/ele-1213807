package com.xiliulou.electricity.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 用户列表(ElectricityConfig)表数据库访问层
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
public interface ElectricityConfigMapper extends BaseMapper<ElectricityConfig>{

    Integer update(ElectricityConfig electricityConfig);
    
    ElectricityConfig selectElectricityConfigByTenantId(Integer tenantId);
    
    Integer updateWxCuStatusByTenantId(ElectricityConfig electricityConfig);

    ElectricityConfig selectByTenantId(@Param("tenantId") Integer tenantId);
}
