package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-12-14:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSourceUpdateQuery {
    @NotNull(message = "用户id不能为空")
    private Long uid;

    @NotNull(message = "用户来源不能为空")
    private Integer source;

    private Long sourceId;
}
