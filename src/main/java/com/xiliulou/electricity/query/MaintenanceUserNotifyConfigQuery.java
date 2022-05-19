package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2022/4/12 09:38
 */
@Data
public class MaintenanceUserNotifyConfigQuery {
    @NotNull(message = "手机号不可以为空")
    private String phones;

    @NotNull(message = "权限不可以为空")
    private Integer permission;
}
