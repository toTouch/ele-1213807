package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentTerminatingRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 14:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstallmentTerminatingRecordVO extends InstallmentTerminatingRecord {
    
    /**
     * 签约金额
     */
    private BigDecimal amount;
    
    /**
     * 未支付金额
     */
    private BigDecimal unpaidAmount;
    
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
