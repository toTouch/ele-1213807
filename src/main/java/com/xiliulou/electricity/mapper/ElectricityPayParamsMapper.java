package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ElectricityPayParamsMapper extends BaseMapper<ElectricityPayParams> {
    @Select("select * from t_electricity_pay_params where tenant_id = #{tenantId}")
    ElectricityPayParams queryByTenantId(@Param("tenantId") Integer tenantId);
}
