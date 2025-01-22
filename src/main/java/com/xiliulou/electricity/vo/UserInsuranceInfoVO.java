package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author renhang
 */
@Data
public class UserInsuranceInfoVO {

    private Integer id;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 城市Id
     */
    private Integer cid;

    /**
     * 保险名称
     */
    private String name;

    /**
     * 保费
     */
    private BigDecimal premium;

    /**
     * 保额
     */
    private BigDecimal forehead;

    /**
     * 可用天数
     */
    private Integer validDays;

    /**
     * 保险类型 0--电池 1--车辆
     */
    private Integer insuranceType;

    /**
     * 状态 0--正常 1--禁用
     */
    private Integer status;

    /**
     * 电池类型套餐
     */
    private String batteryType;

    /**
     * 是否强制购买 0--非强制 1--强制
     */
    private Integer isConstraint;

    /**
     * 删除标志 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 租户id
     */
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;
    /**
     * 门店
     */
    private Long storeId;
    /**
     * 车辆型号
     */
    private Long carModelId;
    /**
     * 电池型号
     */
    private String simpleBatteryType;

    /**
     * 保险说明
     */
    private String instruction;
}
