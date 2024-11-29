/**
 * Create date: 2024/8/28
 */

package com.xiliulou.electricity.service.impl.profitsharing;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.profitsharing.ProfitSharingStatistics;
import com.xiliulou.electricity.enums.YesNoEnum;
import com.xiliulou.electricity.mapper.profitsharing.ProfitSharingStatisticsMapper;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingConfigService;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingStatisticsService;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingCheckVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingConfigVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
    
    @Resource
    private ProfitSharingConfigService profitSharingConfigService;
    
    
    @Override
    public void insert(ProfitSharingStatistics sharingStatistics) {
        long timeMillis = System.currentTimeMillis();
        sharingStatistics.setUpdateTime(timeMillis);
        sharingStatistics.setCreateTime(timeMillis);
        profitSharingStatisticsMapper.insert(sharingStatistics);
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
    
    @Override
    public int subtractAmountById(Long id, BigDecimal rollbackAmount) {
        return profitSharingStatisticsMapper.subtractAmountById(id, rollbackAmount, System.currentTimeMillis());
    }
    
    @Slave
    @Override
    public ProfitSharingCheckVO checkMaxProfitSharingLimit(Integer tenantId, Long franchiseeId) {
        ProfitSharingCheckVO profitSharingCheckVO = new ProfitSharingCheckVO();
        profitSharingCheckVO.setDate(getCurrentDate());
        
        ProfitSharingConfigVO profitSharingConfigVO = profitSharingConfigService.queryByTenantIdAndFranchiseeId(tenantId, franchiseeId);
        if (Objects.isNull(profitSharingConfigVO)) {
            return profitSharingCheckVO;
        }
        
        profitSharingCheckVO.setAmountLimit(profitSharingConfigVO.getAmountLimit());
        
        ProfitSharingStatistics sharingStatistics = queryByTenantIdAndFranchiseeIdAndStatisticsTime(tenantId, franchiseeId, getCurrentMonth());
        
        BigDecimal useAmount = BigDecimal.ZERO;
        if (Objects.nonNull(sharingStatistics)) {
            useAmount = sharingStatistics.getTotalAmount();
        }
        
        profitSharingCheckVO.setUseAmount(useAmount);
        
        if (profitSharingCheckVO.getAmountLimit().compareTo(BigDecimal.ZERO) <= 0) {
            // 金额为0 代表不限制
            profitSharingCheckVO.setIsExceed(YesNoEnum.NO.getCode());
        } else {
            profitSharingCheckVO.setIsExceed(useAmount.compareTo(profitSharingCheckVO.getAmountLimit()) >= 0 ? YesNoEnum.YES.getCode() : YesNoEnum.NO.getCode());
        }
        
        return profitSharingCheckVO;
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
    
    
    /**
     * 获取当前年月日
     *
     * @author caobotao.cbt
     * @date 2024/8/28 17:30
     */
    public String getCurrentDate() {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        
        // 定义日期格式：年-月
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // 格式化当前日期，只保留年和月
        return currentDate.format(formatter);
    }
}
