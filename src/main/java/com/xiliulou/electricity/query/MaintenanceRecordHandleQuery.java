package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2021/9/26 4:17 下午
 */
@Data
public class MaintenanceRecordHandleQuery {
    @NotNull(message = "id不可以为空")
    private Long id;

    private String remark;

    @NotNull(message = "处理状态不能为空")
    private String status;

}
