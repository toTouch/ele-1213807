package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@TableName("t_user_amount")
public class UserAmount {

    private Long id;

    private Long uid;

    private Long createTime;

    private Long updateTime;

    private BigDecimal totalIncome;

    private BigDecimal balance;

    private BigDecimal withdraw;

    private Integer tenantId;

    private Integer delFlg;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
