package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.context.annotation.DeferredImportSelector;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 11:03
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentRecordQuery extends InstallmentRecord {
    
    private Integer offset;
    
    private Integer size;
    
    private List<Integer> statuses;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
}
