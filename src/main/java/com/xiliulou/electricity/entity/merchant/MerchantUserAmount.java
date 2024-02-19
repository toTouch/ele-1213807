package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/2/18 19:28
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_user_amount")
public class MerchantUserAmount {
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 总收入
     */
    private BigDecimal totalIncome;
    
    /**
     * 余额
     */
    private BigDecimal balance;
    
    /**
     * 提现金额
     */
    private BigDecimal withdrawAmount;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    private String remark;
    private Long createTime;
    private Long updateTime;
}
