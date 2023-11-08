package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-18-15:55
 */
@Data
public class CloudBeanUseRecordVO {

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

    private String username;

    private String phone;
    /**
     * 类型 0套餐代付，1套餐回收，2云豆充值，3赠送，4后台充值，5后台扣除
     */
    private Integer type;
    /**
     * 订单类型 0其它，1换电套餐，2换电押金
     */
    private Integer orderType;
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

    private String batteryMemberCard;
    /**
     * 加盟商id
     */
    private Long franchiseeId;

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

    /**
     * 支出
     */
    private BigDecimal expend;
    /**
     * 收入
     */
    private BigDecimal income;
}
