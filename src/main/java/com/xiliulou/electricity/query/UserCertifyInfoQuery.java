package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2024-07-09-11:26
 */
@Data
public class UserCertifyInfoQuery {

    @NotBlank(message = "用户姓名")
    private String name;

    @NotBlank(message = "身份证号")
    private String idNumber;

    @NotBlank(message = "身份证正面照片")
    private String frontPicture;

    @NotBlank(message = "身份证反面照片")
    private String backPicture;
}
