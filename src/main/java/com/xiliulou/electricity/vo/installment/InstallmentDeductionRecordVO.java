package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
