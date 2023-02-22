package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-21-17:34
 */
@Data
public class FreeCarDepositQuery {
    @NotNull(message = "加盟商id不能为空")
    private Long franchiseeId;

    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;

    @NotEmpty(message = "身份证不能为空")
    private String idCard;

    @NotEmpty(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "车辆型号不能为空")
    private Long carModelId;

}
