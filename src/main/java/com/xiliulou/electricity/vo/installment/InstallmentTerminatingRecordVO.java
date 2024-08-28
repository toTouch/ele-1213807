package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 14:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentTerminatingRecordVO extends InstallmentTerminatingRecord {
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 套餐名称
     */
    private String franchiseeName;
    
    /**
     * 审核人姓名
     */
    private String auditorName;
}
