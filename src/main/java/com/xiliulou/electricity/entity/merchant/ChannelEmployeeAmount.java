package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 20:04
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_channel_employee_amount")
public class ChannelEmployeeAmount {
    
    private Long id;
    
    private Long uid;
    
    private BigDecimal totalIncome;
    
    private BigDecimal balance;
    
    private BigDecimal withdrawAmount;
    
    private Integer tenantId;
    
    private Integer delFlag;
    
    private Long createTime;
    
    private Long updateTime;
    
}
