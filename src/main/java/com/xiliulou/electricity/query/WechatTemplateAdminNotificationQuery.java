package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Hardy
 * @date 2021/11/25 17:04
 * @mood
 */
@Data
public class WechatTemplateAdminNotificationQuery {
    private Long id;

    @NotNull(message = "openId不能为空")
    private String openIds;
}
