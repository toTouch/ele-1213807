package com.xiliulou.electricity.query;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-24-15:04
 */
@Data
public class DivisionAccountConfigStatusQuery {

    @NotNull(message = "id不能为空")
    private Long id;

    /**
     * 状态（0-启用，1-禁用）
     */
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态不合法")
    private Integer status;

}
