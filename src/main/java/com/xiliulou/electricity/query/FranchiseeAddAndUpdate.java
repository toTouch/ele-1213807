package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 门店表(TStore)实体类
 *
 * @author makejava
 * @since 2020-12-07 14:59:37
 */
@Data
public class FranchiseeAddAndUpdate {
    /**
    * 加盟商Id
    */
    @NotNull(message = "加盟商Id不能为空!", groups = {UpdateGroup.class})
    private Long id;
    /**
    * 加盟商名称
    */
    @NotEmpty(message = "加盟商名称不能为空!", groups = {CreateGroup.class})
    private String name;
    /**
     * 租电池押金
     */
    private BigDecimal batteryDeposit;


    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class})
    private String password;


    @NotEmpty(message = "手机号的不能为空", groups = {CreateGroup.class})
    private String phone;

    /**
     * 城市编号
     */
    @NotNull(message = "城市编号不能为空!", groups = {CreateGroup.class})
    private Integer cityId;

    /**
     * 省编号
     */
    @NotNull(message = "省编号不能为空!", groups = {CreateGroup.class})
    private Integer provinceId;


    /**
     * 加盟商押金类型 1--老（不分型号） 2--新（分型号）
     * */
    @NotNull(message = "加盟商押金类型不能为空!", groups = {CreateGroup.class})
    private Integer modelType;

    //新分型号押金
    private List<ModelBatteryDeposit> modelBatteryDepositList;

    //新分型号押金
    private String modelBatteryDeposit;

    /**
     * 电池服务费
     */
    private BigDecimal batteryServiceFee;

}
