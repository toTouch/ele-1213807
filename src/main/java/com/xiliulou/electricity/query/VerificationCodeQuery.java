package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-06-28-14:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationCodeQuery {

    private int  size;

    private int offset;
    @NotNull(message = "用户名不能为空")
    private  String userName;
    @NotNull(message = "手机号不能为空")
    private  String phone;

    private  String verificationCode;
}
