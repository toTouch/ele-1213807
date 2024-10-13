package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/9/5 23:20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentDeductionPlanQuery extends InstallmentDeductionPlan {
    
    private List<Long> franchiseeIds;
    
    private List<Integer> statuses;
    
    private Long endTime;
}
