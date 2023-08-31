package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * 换电柜保险(InsuranceUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-02 14:44:12
 */
@Data
public class InsuranceUserInfoVo {

    private Integer id;

    private Long uid;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 保费
     */
    private BigDecimal premium;

    /**
     * 保额
     */
    private BigDecimal forehead;

    /**
     * 保险Id
     */
    private Integer insuranceId;
    /**
     * 保险订单编号
     */
    private String insuranceOrderId;
    private String orderId;

    /**
     * 保险过期时间
     */
    private Long insuranceExpireTime;

    /**
     * 是否出险 0--未出险 1--已出险
     */
    private Integer isUse;

    /**
     * 删除标志 0--正常 1--删除
     */
    private Integer delFlag;

    //租户id
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;
    /**
     * 保险名称
     */
    private String insuranceName;

    private String cityName;

    private Integer cid;

    /**
     * 保险购买时间
     */
    private Long payInsuranceTime;

    /**
     * 保险类型
     */
    private Integer type;
}
