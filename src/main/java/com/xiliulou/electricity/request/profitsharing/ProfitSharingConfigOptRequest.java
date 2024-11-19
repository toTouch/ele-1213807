/**
 *  Create date: 2024/8/23
 */

package com.xiliulou.electricity.request.profitsharing;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/23 10:50
 */
@Data
public class ProfitSharingConfigOptRequest {
    
    @NotNull(message = "id不能为空")
    private Long id;
    
    
    /**
     * 订单类型：1-换电-套餐购买 ，2-换电-保险购买，4-换电-滞纳金缴纳（如同时选择多个类型，则之为类型之和）
     */
    private Integer orderType;
    
    /**
     * 每月最大分账上限
     */
    private BigDecimal amountLimit;
    
    /**
     * 分账类型：1-按订单比例
     */
    private Integer profitSharingType;
    
    /**
     * 允许比例上限
     */
    private BigDecimal scaleLimit;
    
    /**
     * 周期类型：1:D+1
     */
    private Integer cycleType;
    
    /**
     * 租户id
     */
    private Integer tenantId;
}
