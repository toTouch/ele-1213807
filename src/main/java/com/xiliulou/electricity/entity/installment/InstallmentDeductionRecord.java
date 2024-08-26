package com.xiliulou.electricity.entity.installment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 分期代扣记录表实体类
 * @Author: SongJP
 * @Date: 2024/8/26 11:18
 */
@Data
@Builder
@TableName("t_installment_deduction_record")
public class InstallmentDeductionRecord {
    
    @TableId(type = IdType.AUTO, value = "id")
    private Long id;
    
    /**
     * 请求签约用户uid
     */
    private Long uid;
    
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 扣款订单号
     */
    private String payNo;
    
    /**
     * 还款计划号，我方生成的调用接口的参数，不与项目内其他数据关联
     */
    private String repaymentPlanNo;
    
    /**
     * 被扣款人姓名
     */
    private String userName;
    
    /**
     * 被扣款人手机号
     */
    private String mobile;
    
    /**
     * 扣款金额
     */
    private BigDecimal amount;
    
    /**
     * 扣款状态
     */
    private Integer status;
    
    /**
     * 扣款期次
     */
    private Integer issue;
    
    /**
     * 订单标题
     */
    private String subject;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
}
