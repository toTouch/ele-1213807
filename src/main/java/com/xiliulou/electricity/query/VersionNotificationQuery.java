package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2021/9/26 5:44 下午
 */
@Data
public class VersionNotificationQuery {
    @NotNull(message = "版本不能为空")
    private String version;
    @NotNull(message = "内容不能为空")
    private String content;

    private Integer id;
}
