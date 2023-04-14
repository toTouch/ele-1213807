package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * (FreeDepositAlipayHistory)实体类
 *
 * @author zgw
 * @since 2023-04-13 09:12:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_free_deposit_alipay_history")
public class FreeDepositAlipayHistory {
    
    private Long id;
    
    private String orderId;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private String idCard;
    
    private Long operateUid;
    
    private String operateName;
    
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 代扣金额
     */
    private BigDecimal alipayAmount;
    
    private Integer type;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Integer tenantId;
    
    private Integer payStatus;
    
    private String remark;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
