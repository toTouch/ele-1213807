/**
 * Copyright(c) 2018 Sunyur.com, All Rights Reserved. Author: sunyur Create date: 2024/8/28
 */

package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingStatisticsMapper;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingStatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/28 17:21
 */
@Service
public class ProfitSharingStatisticsServiceImpl implements ProfitSharingStatisticsService {
    
    @Resource
    private ProfitSharingStatisticsMapper profitSharingStatisticsMapper;
    
    @Override
    public void addTotalAmount(Long id, BigDecimal amount) {
        profitSharingStatisticsMapper.addTotalAmount(amount, id, System.currentTimeMillis());
    }
    
    @Override
    public void insert(ProfitSharingStatistics sharingStatistics) {
    
    }
    
    @Override
    public ProfitSharingStatistics queryByCurrentMonth(Integer tenantId, Long franchiseeId) {
        return queryByTenantIdAndFranchiseeIdAndStatisticsTime(tenantId, franchiseeId, getCurrentMonth());
    }
    
    @Slave
    @Override
    public ProfitSharingStatistics queryByTenantIdAndFranchiseeIdAndStatisticsTime(Integer tenantId, Long franchiseeId, String statisticsTime) {
        return profitSharingStatisticsMapper.selectByTenantIdAndFranchiseeId(tenantId, franchiseeId, statisticsTime);
    }
    
    
    /**
     * 获取当前月
     *
     * @author caobotao.cbt
     * @date 2024/8/28 17:30
     */
    @Override
    public String getCurrentMonth() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        
        // 定义日期格式：年-月
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        // 格式化当前日期，只保留年和月
        return currentDate.format(formatter);
    }
}
