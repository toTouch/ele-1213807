package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ElectricityPayParamsMapper extends BaseMapper<ElectricityPayParams> {
    
    /**
     * 根据租户id + 加盟商id集合查询
     *
     * @param tenantId
     * @param franchiseeIds
     * @author caobotao.cbt
     * @date 2024/6/12 13:51
     */
    List<ElectricityPayParams> selectListByTenantIdAndFranchiseeIds(@Param("tenantId") Integer tenantId, @Param("franchiseeIds") List<Long> franchiseeIds);
    
    
    /**
     * 更新
     *
     * @author caobotao.cbt
     * @date 2024/6/12 13:51
     */
    int update(ElectricityPayParams update);
    
    /**
     * 逻辑删除
     *
     * @author caobotao.cbt
     * @date 2024/6/12 13:51
     */
    int logicalDelete(@Param("id") Long id, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据租户id查询全部
     *
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/6/12 18:12
     */
    List<ElectricityPayParams> selectByTenantId(@Param("tenantId") Integer tenantId);
}
