package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-06-05-14:41
 */
@Data
public class InvitationActivityStatusQuery {
    @NotBlank(message = "活动id不能为空")
    private Long id;
    @NotBlank(message = "活动状态能为空")
    private Integer status;
}
