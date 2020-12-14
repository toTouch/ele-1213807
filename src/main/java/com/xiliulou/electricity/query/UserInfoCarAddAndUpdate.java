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
public class UserInfoCarAddAndUpdate {
    @NotNull(message = "Id不能为空!", groups = {UpdateGroup.class})
    private Long id;
    /**
    * 用户姓名
    */
    @NotEmpty(message = "用户姓名不能为空!", groups = {UpdateGroup.class})
    private String name;

    private Integer carStoreId;
    /**
    * 身份证号
    */
    @NotEmpty(message = "身份证号不能为空!", groups = {UpdateGroup.class})
    private String idNumber;
    /**
     *  车辆编号
     */
    @NotEmpty(message = " 车辆编号不能为空!", groups = {UpdateGroup.class})
    private String carSn;
    /**
     *车牌号
     */
    @NotEmpty(message = "车牌号不能为空!", groups = {UpdateGroup.class})
    private String numberPlate;
    /**
    * 租车押金
    */
    @NotNull(message = "租车押金不能为空!", groups = {UpdateGroup.class})
    private Double carDeposit;

}