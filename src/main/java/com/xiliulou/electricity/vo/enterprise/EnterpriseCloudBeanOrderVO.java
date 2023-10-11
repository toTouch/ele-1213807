package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-09-15-9:34
 */
@Data
public class EnterpriseCloudBeanOrderVO {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    private Long businessId;

    private String enterpriseName;
    /**
     * 用户id
     */
    private Long uid;

    private String username;

    private String operateName;
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
     * 操作类型 0:赠送,1:后台充值,2:后台扣除
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

    private String franchiseeName;
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
}
