package com.xiliulou.electricity.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 缴纳押金订单表(TEleDepositOrder)实体类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_deposit_order")
public class EleDepositOrder {
    
    private Long id;
    /**
    * 支付金额
    */
    private BigDecimal payAmount;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
    * 用户Id
    */
    private Long uid;
    /**
    * 订单Id
    */
    private String orderId;
    /**
    * 状态（0、未支付,1、支付成功,2、支付失败）
    */
    private Integer status;
    /**
    * 用户名
    */
    private String userName;
    /**
    * 手机号
    */
    private String phone;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 0;


}