package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/20 11:07
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_withdraw_application")
public class MerchantWithdrawApplication {
    
    /**
     * 提现申请ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 提现人UID
     */
    private Long uid;
    
    /**
     * 提现订单号
     */
    private String orderNo;
    
    /**
     * 银行名称
     */
    private String bankName;
    
    /**
     * 银行卡号
     */
    private String bankNumber;
    
    /**
     * 银行编号
     */
    private String bankCode;
    
    /**
     * 银行卡账户
     */
    private String accountName;
    
    /**
     * 提现金额， 单位元
     */
    private BigDecimal amount;
    
    /**
     * 手续费，单位元
     */
    private BigDecimal handlingFee;
    
    /**
     * 审核时间
     */
    private Long checkTime;
    
    /**
     * 审核人
     */
    private Long operator;
    
    /**
     * 到账时间
     */
    private Long receiptTime;
    
    /**
     * 审核状态 1--审核中 2--审核拒绝  3--审核通过  4--提现中  5--提现成功  6--提现失败
     */
    private Integer status;
    
    /**
     * 提现发起的批次号
     */
    private String batchNo;
    
    /**
     * 支付机构返回的交易批次编号
     */
    private String transactionBatchId;
    
    /**
     * 提现类型 1--微信 2--宝付
     */
    private Integer withdrawType;
    
    /**
     * 第三方提现返回结果
     */
    private String response;
    
    /**
     * 删除标记 (0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 支付类型配置：0，默认，1：加盟商
     */
    private Integer payConfigType;
    
    /**
     * 微信商户号
     */
    private String wechatMerchantId;
    
    /**
     * 支付配置是否改变;0 否， 1 是
     */
    private Integer payConfigWhetherChange;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
}
