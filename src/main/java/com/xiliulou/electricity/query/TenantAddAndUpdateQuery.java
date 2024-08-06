package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lxc
 * @date 2021/06/23 0029 17:01
 * @Description:
 */
@Data
public class TenantAddAndUpdateQuery {

    @NotNull(groups = UpdateGroup.class, message = "租户ID不能为空")
    private Integer id;

    @NotBlank(groups = {UpdateGroup.class,CreateGroup.class}, message = "租户名称不能为空")
    private String name;

    private String code;

    @NotBlank(groups = CreateGroup.class, message = "密码不能为空")
    private String password;

    @NotBlank(groups = CreateGroup.class, message = "手机号不能为空")
    private String phone;

    private Integer status;
    private Integer delFlag;
    private Long createTime;
    private Long updateTime;

    private Long expireTime;
}
