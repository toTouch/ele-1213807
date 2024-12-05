package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/12/5 10:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstallmentDeductionPlanAssemblyVO extends InstallmentDeductionPlan {
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 未支付金额
     */
    private BigDecimal unPaidAmount;
    
    /**
     * 该期是否已全部代扣完成，0-未全部代扣完成，1-全部代扣完成
     */
    private Integer completionStatus;
}
