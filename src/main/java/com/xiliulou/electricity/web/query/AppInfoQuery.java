package com.xiliulou.electricity.web.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author : eclair
 * @date : 2021/7/21 10:00 上午
 */
@Data
public class AppInfoQuery {
    @NotNull(message = "应用类型不能为空")
    private String type;
}
