package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/27 15:32
 */

@Data
public class EnterpriseRefundDepositOrderVO {
    
    private Long id;
    
    /**
     * 骑手UID
     */
    private Long uid;
    
    /**
     * 用户名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 退押单号
     */
    private String depositOrderNo;
    
    /**
     * 支付单号
     */
    private String orderNo;
    
    /**
     * 支付金额, 单位元
     */
    private BigDecimal payAmount;
    
    /**
     * 退款金额, 单位元
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款状态
     */
    private Integer status;
    
    private Integer payType;
    
    /**
     *  退款类型 0--电池押金退款 1--租车押金退款
     */
    private Integer refundOrderType;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 套餐名称
     */
    private String packageName;
    
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    
}
