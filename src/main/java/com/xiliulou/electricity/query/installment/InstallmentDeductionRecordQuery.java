package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.entity.installment.InstallmentDeductionRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 17:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentDeductionRecordQuery extends InstallmentDeductionRecord {
    
    private Integer offset;
    
    private Integer size;
    
    private List<Long> franchiseeIds;
}
