package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2021/9/26 3:07 下午
 */
@Data
public class UserMaintenanceQuery {
    private String remark;

    @NotNull(message = "故障类型不能为空")
    private String type;

    private String filepath;

    @NotNull(message = "柜机Id不能为空")
    private Integer electricityCabinetId;

}

