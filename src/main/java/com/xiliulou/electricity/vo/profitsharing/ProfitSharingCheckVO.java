/**
 * Create date: 2024/9/2
 */

package com.xiliulou.electricity.vo.profitsharing;

import lombok.Data;

import java.math.BigDecimal;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/9/2 17:40
 */
@Data
public class ProfitSharingCheckVO {
    
    /**
     * 最大限额
     */
    private BigDecimal amountLimit;
    
    
    /**
     * 已使用金额
     */
    private BigDecimal useAmount;
    
    /**
     * 时间
     */
    private String date;
    
    /**
     * 是否超出
     *
     * @see com.xiliulou.electricity.enums.YesNoEnum
     */
    private Integer isExceed;
    
}
