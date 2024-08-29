/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/28
 */

package com.xiliulou.electricity.service.profitsharing;

import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingStatisticsMapper;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/28 17:19
 */
public interface ProfitSharingStatisticsService {
    
    /**
     * 金额累计
     *
     * @param id
     * @param amount
     * @author caobotao.cbt
     * @date 2024/8/28 17:20
     */
    void addTotalAmount(Long id, BigDecimal amount);
    
    /**
     * 新增
     *
     * @param sharingStatistics
     * @author caobotao.cbt
     * @date 2024/8/28 17:37
     */
    void insert(ProfitSharingStatistics sharingStatistics);
    
    /**
     * 获取当前月
     *
     * @author caobotao.cbt
     * @date 2024/8/28 17:43
     */
    String getCurrentMonth();
    
    /**
     * 查询当前月
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/28 17:21
     */
    ProfitSharingStatistics queryByCurrentMonth(Integer tenantId, Long franchiseeId);
    
    /**
     * 根据租户+加盟商+时间查询
     *
     * @param tenantId
     * @param franchiseeId
     * @author caobotao.cbt
     * @date 2024/8/28 17:21
     */
    ProfitSharingStatistics queryByTenantIdAndFranchiseeIdAndStatisticsTime(Integer tenantId, Long franchiseeId, String statisticsTime);
    
    int subtractAmountById(Long id, BigDecimal rollbackAmount);
}