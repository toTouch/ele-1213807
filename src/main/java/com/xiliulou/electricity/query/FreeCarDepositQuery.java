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
//    @NotNull(message = "加盟商id不能为空")
//    private Long franchiseeId;

    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;

    @NotNull(message = "车辆型号不能为空!")
    private Long carModelId;

    @NotEmpty(message = "身份证不能为空")
    private String idCard;

    @NotEmpty(message = "真实姓名不能为空")
    private String realName;

    private Integer memberCardId;

//    @NotNull(message = "门店不能为空!")
//    private Long storeId;


//    @NotNull(message = "车辆租赁时间不能为空!")
//    private Integer rentTime;
//
//    @NotBlank(message = "车辆租赁方式不能为空!")
//    private String rentType;


}
