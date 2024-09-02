package com.xiliulou.electricity.vo.meituan;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 订单
 * @date 2024/8/29 14:26:38
 */
@Data
public class OrderVO {
    
    private Long id;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 订单ID
     */
    private String meiTuanOrderId;
    
    /**
     * 下单时间
     */
    private Long meiTuanOrderTime;
    
    /**
     * 实付价
     */
    private BigDecimal meiTuanActuallyPayPrice;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 订单使用状态:0-未使用 1-已使用
     */
    private Integer orderUserStatus;
}
