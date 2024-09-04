package com.xiliulou.electricity.entity.installment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description 分期代扣计划表实体类
 * @Author: SongJP
 * @Date: 2024/8/26 11:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_installment_deduction_plan")
public class InstallmentDeductionPlan {
    
    @TableId(type = IdType.AUTO, value = "id")
    private Long id;
    
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 扣款订单号，关联对应的最新一条代扣记录
     */
    private String payNo;
    
    /**
     * 分期期次
     */
    private Integer issue;
    
    /**
     * 应还款金额
     */
    private BigDecimal amount;
    
    /**
     * 分期套餐id
     */
    private Long packageId;
    
    /**
     * 分期套餐类型，0-换电，1-租车，2-车电一体
     */
    private Integer packageType;
    
    /**
     * 租期，单位天
     */
    private Integer rentTime;
    
    /**
     * 应还款时间
     */
    private Long deductTime;
    
    /**
     * 应还款时间
     */
    private Long paymentTime;
    
    /**
     * 支付状态
     */
    private Integer status;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
}
