package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-07-16:22
 */
@Data
public class BatteryMemberCardStatusQuery {
    @NotNull(message = "id不能为空")
    private Long id;
    @NotNull(message = "状态能为空")
    private Integer status;
}
