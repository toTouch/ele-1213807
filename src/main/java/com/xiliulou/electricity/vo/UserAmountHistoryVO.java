package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * (FranchiseeSplitAccountHistory)实体类
 *
 * @author lxc
 * @since 2021-09-13 20:09:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAmountHistoryVO {

    private Long id;

    private Long uid;

    /**
     * 收益来源 1--邀人返现
     */
    private Integer type;


    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 邀人uid
     */
    private Long joinUid;

    /**
     * 邀人phone
     */
    private String joinPhone;


    private Long createTime;

    private Integer tenantId;



}
