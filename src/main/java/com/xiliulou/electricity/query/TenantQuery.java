package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author lxc
 * @date 2021/06/23 0029 17:01
 * @Description:
 */
@Data
public class TenantQuery {

    private Integer id;

    @NotNull(groups = CreateGroup.class, message = "租户名称不能为空")
    private String name;

    private String code;

    @NotNull(groups = CreateGroup.class, message = "密码不能为空")
    private String password;

    @NotNull(groups = CreateGroup.class, message = "手机号不能为空")
    private String phone;

    private Integer status;
    private Integer delFlag;
    private Long createTime;
    private Long updateTime;
}
