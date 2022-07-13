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
    @NotNull(message = "uId不能为空!", groups = {UpdateGroup.class})
    private Long uid;
    /**
    * 初始电池编号
    */
    @NotEmpty(message = "初始电池编号不能为空!", groups = {UpdateGroup.class})
    private String initElectricityBatterySn;





}
