package com.xiliulou.electricity.query.installment;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InstallmentSignQuery {

    /**
     * 请求签约用户uid
     */
    @NotNull(message = "购买用户数据不能为空!")
    private Long uid;

    /**
     * 实际签约人姓名
     */
    @NotNull(message = "签约人姓名不能为空!")
    private String userName;

    /**
     * 实际签约人手机号
     */
    @NotNull(message = "签约人手机号不能为空!")
    private String mobile;
}
