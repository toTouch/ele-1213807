package com.xiliulou.electricity.vo.profitsharing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author maxiaodong
 * @date 2024/8/23 10:20
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitSharingOrderDetailVO {
    private Long id;
    
    /**
     * 分账单号
     */
    private String orderNo;
    
    /**
     * 第三方支付订单号
     */
    private String thirdTradeOrderNo;
    
    /**
     * 订单金额,单位元
     */
    private BigDecimal amount;
    
    /**
     * 分账类型 0:默认，1：加盟商
     */
    private Integer outAccountType;
    
    /**
     * 分账方
     */
    private String profitSharingOutAccount;
    
    /**
     * 分账接收方
     */
    private String profitSharingReceiveAccount;
    
    /**
     * 分账比例
     */
    private BigDecimal scale;
    
    /**
     * 分账金额
     */
    private BigDecimal profitSharingAmount;
    
    /**
     * 分账类型：0：分出、1：解冻
     */
    private Integer type;
    
    /**
     * 分账状态：0：已受理、1：处理中、2：分账完成，3：分账失败
     */
    private Integer status;
    
    /**
     * 失败原因
     */
    private String failReason;
    
    /**
     * 业务类型：0：换电-套餐购买、1：换电-保险购买、2：换电-滞纳金缴纳、3：换电-押金缴纳, 98: 解冻，99：系统级别
     */
    private Integer businessType;
    
    /**
     * 业务订单号
     */
    private String businessOrderNo;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 分账完成时间
     */
    private Long finishTime;
}
