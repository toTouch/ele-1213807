package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;


/**
 * 租车套餐订单表(CarMemberCardOrder)表实体类
 *
 * @author zzlong
 * @since 2022-12-21 09:47:24
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_car_member_card_order")
public class CarMemberCardOrder {

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
     * 状态（0,未支付,1,支付成功 2,支付失败）
     */
    private Integer status;
    /**
     * 车辆型号id
     */
    private Long carModelId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 套餐类型
     */
    private String memberCardType;
    /**
     * 套餐名称
     */
    private String cardName;
    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 有效天数
     */
    private Integer validDays;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;

    public static final Integer ONLINE_PAYTYPE = 0;
    public static final Integer OFFLINE_PAYTYPE = 1;

}
