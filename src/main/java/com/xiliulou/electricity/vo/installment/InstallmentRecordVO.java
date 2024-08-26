package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 11:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentRecordVO extends InstallmentRecord {
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 套餐名称
     */
    private String franchiseeName;
}
