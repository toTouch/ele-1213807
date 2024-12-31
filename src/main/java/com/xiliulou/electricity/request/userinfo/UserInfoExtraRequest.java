package com.xiliulou.electricity.request.userinfo;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class UserInfoExtraRequest {

    @NotBlank(message = "备注不能为空")
    @Length(min = 1, max = 100, message = "长度最大支持100字", groups = {CreateGroup.class, UpdateGroup.class})
    private String remark;

    private Long uid;
}
