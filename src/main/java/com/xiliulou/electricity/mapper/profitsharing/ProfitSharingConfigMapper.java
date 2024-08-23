package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 分账方配置表(TProfitSharingConfig)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-22 17:14:17
 */
public interface ProfitSharingConfigMapper {
    
    
    /**
     * 新增数据
     *
     * @param profitSharingConfig 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingConfig profitSharingConfig);
    
    
    /**
     * 修改数据
     *
     * @param profitSharingConfig 实例对象
     * @return 影响行数
     */
    int update(ProfitSharingConfig profitSharingConfig);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    /**
     * 根据支付配置id查询
     *
     * @param payParamsId
     * @param tenantId
     * @author caobotao.cbt
     * @date 2024/8/22 18:36
     */
    ProfitSharingConfig selectByPayParamsIdAndTenantId(@Param("payParamsId") Integer payParamsId, @Param("tenantId") Integer tenantId);
    
    /**
     * 根据租户id+加盟商查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/23 09:04
     */
    ProfitSharingConfig selectByTenantIdAndFranchiseeId(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId);
    
    /**
     * 根据租户id+id查询
     *
     * @param tenantId
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/23 10:13
     */
    ProfitSharingConfig selectByTenantIdAndId(@Param("tenantId") Integer tenantId, @Param("id") Long id);
    
    
    /**
     * 根据id更新状态
     *
     * @param id
     * @param configStatus
     * @author caobotao.cbt
     * @date 2024/8/23 10:22
     */
    int updateConfigStatusById(@Param("id") Long id, @Param("configStatus") Integer configStatus, @Param("updateTime") Long updateTime);
    
}

