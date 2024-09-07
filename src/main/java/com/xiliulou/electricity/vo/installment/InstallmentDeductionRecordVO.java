package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 17:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentDeductionRecordVO extends InstallmentDeductionRecord {
    
    /**
     * 套餐名称
     */
    private String franchiseeName;
}
