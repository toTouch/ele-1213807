package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * (UserAmount)实体类
 *
 * @author Eclair
 * @since 2021-05-06 20:09:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAmountVO {

    private Long id;

    private Long uid;

    private String name;

    private Long createTime;

    private Long updateTime;

    private BigDecimal totalIncome;

    private BigDecimal balance;

    private BigDecimal withdraw;

    private Integer tenantId;

    private Integer delFlg;

    private String phone;

}
