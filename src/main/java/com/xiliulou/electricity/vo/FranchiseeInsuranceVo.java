package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 加盟商保险(FranchiseeInsurance)实体类
 *
 * @author makejava
 * @since 2022-11-03 14:59:37
 */
@Data
public class FranchiseeInsuranceVo {

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
     * 是否强制购买 0--非强制 1--强制
     */
    private Integer isConstraint;

    /**
     * 删除标志 0--正常 1--删除
     */
    private Integer delFlag;

    /**
     * 电池类型套餐
     */
    private String batteryType;

    /**
     * 保险说明
     */
    private String instruction;

    private Long createTime;

    private Long updateTime;

    /**
     * 城市名称
     */
    private String cityName;
}
