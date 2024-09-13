package com.xiliulou.electricity.mapper.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 分账统计(TProfitSharingStatistics)表数据库访问层
 *
 * @author makejava
 * @since 2024-08-22 17:31:15
 */
public interface ProfitSharingStatisticsMapper {
    
    
    /**
     * 根据租户id+加盟商id查询
     *
     * @param tenantId
     * @param franchiseeId
     * @param statisticsTime
     * @author caobotao.cbt
     * @date 2024/8/28 16:55
     */
    ProfitSharingStatistics selectByTenantIdAndFranchiseeId(@Param("tenantId") Integer tenantId, @Param("franchiseeId") Long franchiseeId, @Param("statisticsTime") String statisticsTime);
    
    
    /**
     * 累加金额
     *
     * @param amount
     * @param id
     * @param updateTime
     * @author caobotao.cbt
     * @date 2024/8/28 17:17
     */
    int addTotalAmount(@Param("amount") BigDecimal amount, @Param("id") Long id, @Param("updateTime") Long updateTime);
    
    /**
     * 新增数据
     *
     * @param profitSharingStatistics 实例对象
     * @return 影响行数
     */
    int insert(ProfitSharingStatistics profitSharingStatistics);
    
    
    int subtractAmountById(Long id, BigDecimal rollbackAmount, long currentTimeMillis);
}

