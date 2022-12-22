package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;


/**
 * (CarDepositOrder)表实体类
 *
 * @author zzlong
 * @since 2022-12-21 09:15:22
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_deposit_order")
public class CarDepositOrder {

    private Long id;
    /**
     * 用户Id
     */
    private Long uid;
    /**
     * 订单Id
     */
    private String orderId;
    /**
     * 用户名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 状态（0、未支付,1、支付成功,2、支付失败）
     */
    private Integer status;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     * 车辆型号Id
     */
    private Long carModelId;
    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;

    public static final Integer ONLINE_PAYTYPE = 0;
    public static final Integer OFFLINE_PAYTYPE = 1;

}
