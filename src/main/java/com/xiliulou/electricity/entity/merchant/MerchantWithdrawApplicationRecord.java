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
 * @description: 提现发起批次记录表
 * @date 2024/2/24 13:58
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_withdraw_application_record")
public class MerchantWithdrawApplicationRecord {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long uid;
    
    private String orderNo;
    
    private String batchNo;
    
    private String batchDetailNo;
    
    private String transaction_no;
    
    private BigDecimal amount;
    
    private Integer status;
    
    private Integer tenantId;
    
    private String remark;
    
    private String response;
    
    private Long createTime;
    
    private Long updateTime;

}
