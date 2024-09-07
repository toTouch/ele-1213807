package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 14:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentTerminatingRecordQuery  extends InstallmentTerminatingRecord {
    
    private Integer offset;
    
    private Integer size;
    
    private List<Integer> statuses;
    
    private List<Long> franchiseeIds;
}
