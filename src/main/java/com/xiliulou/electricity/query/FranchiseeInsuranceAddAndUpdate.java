package com.xiliulou.electricity.query;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 加盟商保险(FranchiseeInsurance)实体类
 *
 * @author makejava
 * @since 2022-11-03 14:59:37
 */
@Data
public class FranchiseeInsuranceAddAndUpdate {

    @NotNull(message = "保险Id不能为空!", groups = {UpdateGroup.class})
    private Integer id;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 城市Id
     */
    @NotNull(message = "城市不能为空!")
    private Integer cid;

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

    /**
     * 保险说明
     */
    @NotEmpty(message = "保险说明不能为空!")
    private String instruction;
}
