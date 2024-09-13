

package com.xiliulou.electricity.request.profitsharing;

import com.xiliulou.electricity.enums.profitsharing.ProfitSharingConfigStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/22 19:10
 */
@Data
public class ProfitSharingConfigUpdateStatusOptRequest {
    
    
    @NotNull(message = "franchiseeId 不能为空")
    private Long franchiseeId;
    
    
    private Integer tenantId;
    
    /**
     * @see ProfitSharingConfigStatusEnum
     */
    private Integer configStatus;
}
