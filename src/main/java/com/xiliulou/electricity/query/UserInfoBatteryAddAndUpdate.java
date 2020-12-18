package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 用户列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Data
public class UserInfoBatteryAddAndUpdate {
    @NotNull(message = "Id不能为空!", groups = {UpdateGroup.class})
    private Long id;
    /**
    * 用户姓名
    */
    @NotEmpty(message = "用户姓名不能为空!", groups = {UpdateGroup.class})
    private String name;

    private Integer batteryStoreId;
    /**
    * 身份证号
    */
    @NotEmpty(message = "身份证号不能为空!", groups = {UpdateGroup.class})
    private String idNumber;
    /**
    * 初始电池编号
    */
    @NotEmpty(message = "初始电池编号不能为空!", groups = {UpdateGroup.class})
    private String initElectricityBatterySn;
    /**
    * 租电池押金
    */
    @NotNull(message = "租电池押金不能为空!", groups = {UpdateGroup.class})
    private Double batteryDeposit;



}