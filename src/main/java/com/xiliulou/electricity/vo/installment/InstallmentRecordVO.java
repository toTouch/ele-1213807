package com.xiliulou.electricity.vo.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    /**
     * 分期套餐首期费用
     */
    private BigDecimal downPayment;
    
    /**
     * 是否逾期，0-正常，1-逾期
     */
    private Integer overdue;
    
    /**
     * 是否正在解约审核，0-正常，1-审核中
     */
    private Integer underReview;
    
    /**
     * 租金
     */
    private BigDecimal rentPrice;
    
    /**
     * 剩余每期金额
     */
    private BigDecimal remainingPrice;
    
    /**
     * 未支付期数
     */
    private Integer unpaidInstallmentNo;
    
    /**
     * 未支付金额
     */
    private BigDecimal unpaidAmount;
    
    /**
     * 值为1时，解约申请被拒绝过
     */
    private Integer refused;
    
    /**
     * 解约申请被拒绝的最新的原因
     */
    private String opinion;
    
    /**
     * 套餐租期
     */
    private Integer validDays;
}
