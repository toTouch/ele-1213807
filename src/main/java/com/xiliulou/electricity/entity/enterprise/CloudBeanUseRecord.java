package com.xiliulou.electricity.entity.enterprise;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 云豆使用记录表(CloudBeanUseRecord)实体类
 *
 * @author Eclair
 * @since 2023-09-18 10:35:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_cloud_bean_use_record")
public class CloudBeanUseRecord {
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
    /**
     * 类型 0套餐代付，1套餐回收，2云豆充值，3赠送，4后台充值，5后台扣除
     */
    private Integer type;
    /**
     * 本次使用云豆数量
     */
    private BigDecimal beanAmount;
    /**
     * 剩余云豆数量
     */
    private BigDecimal remainingBeanAmount;
    /**
     * 套餐id
     */
    private Long packageId;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 充值订单号/代付订单号/退电订单号
     */
    private String ref;
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

    //类型 0套餐代付，1套餐回收，2云豆充值，3赠送，4后台充值，5后台扣除
    public static final Integer TYPE_PAY_MEMBERCARD = 0;
    public static final Integer TYPE_RECYCLE = 1;
    public static final Integer TYPE_USER_RECHARGE = 2;
    public static final Integer TYPE_PRESENT = 3;
    public static final Integer TYPE_ADMIN_RECHARGE = 4;
    public static final Integer TYPE_ADMIN_DEDUCT = 5;

}
