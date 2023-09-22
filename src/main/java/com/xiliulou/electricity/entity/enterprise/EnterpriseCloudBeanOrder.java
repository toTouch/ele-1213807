package com.xiliulou.electricity.entity.enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 企业云豆充值订单表(EnterpriseCloudBeanOrder)实体类
 *
 * @author Eclair
 * @since 2023-09-15 09:29:15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_cloud_bean_order")
public class EnterpriseCloudBeanOrder {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 企业id
     */
    private Long enterpriseId;
    /**
     * 用户id
     */
    private Long uid;

    private Long operateUid  ;
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    /**
     * 订单Id
     */
    private String orderId;
    /**
     * 状态（0,未支付,1,支付成功 2,支付失败）
     */
    private Integer status;
    /**
     * 交易方式 0--线上 1--线下
     */
    private Integer payType;
    /**
     * 操作类型 0:套餐代付,1:余额回收,2:云豆充值,3:赠送,4:后台充值,5:后台扣除
     */
    private Integer type;
    /**
     * 云豆数量
     */
    private BigDecimal beanAmount;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 租户ID
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * 备注
     */
    private String remark;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    // 订单状态 0未支付,1支付成功 2支付失败
    public static final Integer STATUS_INIT = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAIL = 2;

    //操作类型 0:套餐代付,1:余额回收,2:云豆充值,3:赠送,4:后台充值,5:后台扣除
    public static final Integer TYPE_PAY_MEMBERCARD = 0;
    public static final Integer TYPE_RECYCLE = 1;
    public static final Integer TYPE_USER_RECHARGE = 2;
    public static final Integer TYPE_PRESENT = 3;
    public static final Integer TYPE_ADMIN_RECHARGE = 4;
    public static final Integer TYPE_ADMIN_DEDUCT = 5;

    //交易方式 0--线上 1--线下
    public static final Integer ONLINE_PAYMENT = 0;
    public static final Integer OFFLINE_PAYMENT = 1;

}
