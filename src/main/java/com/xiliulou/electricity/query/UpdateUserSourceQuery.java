package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-14-10:13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserSourceQuery {
    @NotNull(message = "用户id不能为空")
    private Long uid;

    @NotNull(message = "用户来源不能为空")
    private Integer source;

    private Long refId;

    private Integer tenantId;


}
