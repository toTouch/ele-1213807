package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-30-16:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OldUserActivityUpdateQuery {

    @NotNull(message = "活动id不能为空!")
    private Integer id;
    /**
     * 活动名称
     */
    @NotEmpty(message = "活动名称不能为空!")
    private String name;

    /**
     * 用户范围
     */
    @NotNull(message = "用户范围不能为空!")
    private Integer userScope;

}
