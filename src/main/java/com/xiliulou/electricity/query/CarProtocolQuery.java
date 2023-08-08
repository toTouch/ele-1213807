package com.xiliulou.electricity.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 13:48
 * @Description:
 */
@Data
public class CarProtocolQuery {

    private Long id;

    @NotNull(message = "协议内容不能为空")
    private String content;

}
