/**
 * Create date: 2024/9/2
 */

package com.xiliulou.electricity.controller.admin.profitsharing;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingStatisticsService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingCheckVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/2 17:32
 */
@RestController
public class JsonAdminProfitSharingStatisticsController {
    
    
    @Resource
    private ProfitSharingStatisticsService profitSharingStatisticsService;
    
    
    @GetMapping("/admin/profitSharingStatistics/{franchiseeId}")
    public R profitSharingStatisticsService(@PathVariable("franchiseeId") Long franchiseeId) {
        ProfitSharingCheckVO profitSharingCheckVO = profitSharingStatisticsService.checkMaxProfitSharingLimit(TenantContextHolder.getTenantId(), franchiseeId);
        return R.ok(profitSharingCheckVO);
    }
    
    
}
