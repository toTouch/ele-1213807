package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 换电柜保险(FranchiseeInsurance)实体类
 *
 * @author makejava
 * @since 2022-11-02 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee_insurance")
public class FranchiseeInsurance {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 保险名称
     */
    @NotEmpty(message = "保险名称不能为空!")
    private String name;

    /**
     * 保费
     */
    @NotNull(message = "保费不能为空!")
    private BigDecimal premium;

    /**
     * 保额
     */
    @NotNull(message = "保额不能为空!")
    private BigDecimal forehead;

    /**
     * 可用天数
     */
    @NotNull(message = "可用天数不能为空!")
    private Integer validDays;

    /**
     * 保险类型 0--电池 1--车辆
     */
    @NotNull(message = "保险类型不能为空!")
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

    //租户id
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;


    //禁用状态
    public static final Integer STATUS_UN_USABLE = 1;
    //可用状态
    public static final Integer STATUS_USABLE = 0;

}
