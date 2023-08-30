package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-15-11:30
 */
@Data
public class FreeBatteryDepositQueryV3 {
    @NotNull(message = "套餐id不能为空")
    private Long membercardId;

    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;

    @NotEmpty(message = "身份证不能为空")
    private String idCard;

    @NotEmpty(message = "真实姓名不能为空")
    private String realName;

    private String productKey;

    private String deviceName;

}
