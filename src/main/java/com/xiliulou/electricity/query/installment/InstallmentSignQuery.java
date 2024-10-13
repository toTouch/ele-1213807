package com.xiliulou.electricity.query.installment;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author 15927
 */
@Data
public class InstallmentSignQuery {
    /**
     * 实际签约人姓名
     */
    private String userName;

    /**
     * 实际签约人手机号
     */
    private String mobile;
}
