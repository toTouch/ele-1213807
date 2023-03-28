package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * (CarRefundOrder)实体类
 *
 * @author Eclair
 * @since 2023-03-15 13:41:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_refund_order")
public class CarRefundOrder {
    
    private Long id;
    
    private String orderId;
    
    private String depositOrderId;
    
    private Long uid;
    
    private String name;
    
    private String phone;
    
    private Long carId;
    
    private String carSn;
    
    private BigDecimal carDeposit;
    
    private Long carModelId;
    
    /**
     * 1--审核中 2--审核通过 3--审核拒绝
     */
    private Integer status;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;
    
    private Long storeId;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;
    
    public static final Integer STATUS_INIT = 1;
    
    public static final Integer STATUS_SUCCESS = 2;
    
    public static final Integer STATUS_FAIL = 3;
}
