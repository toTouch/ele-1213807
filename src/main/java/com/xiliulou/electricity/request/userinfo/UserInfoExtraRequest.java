package com.xiliulou.electricity.request.userinfo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;


/**
 * @Description: UserInfoExtraRequest
 * @Author: renhang
 * @Date: 2025/01/23
 */

@Data
public class UserInfoExtraRequest {

    @Length(min = 1, max = 100, message = "长度最大支持100字")
    private String remark;

    private Long uid;
}
