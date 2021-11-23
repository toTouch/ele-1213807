package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("t_user_amount_history")
public class UserAmountHistory {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    /**
     * 收益来源 1--邀人返现
     */
    private Integer type;


    /**
     * 金额
     */
    private BigDecimal amount;

    private Long createTime;

    private Integer tenantId;


    public static final Integer TYPE_SHARE_ACTIVITY = 1;

}
