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
 * (StoreAmount)实体类
 *
 * @author lxc
 * @since 2021-09-13 20:09:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_store_amount")
public class StoreAmount {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private Long uid;

    private Long storeId;

    private Long createTime;

    private Long updateTime;

    private BigDecimal totalIncome;

    private BigDecimal balance;

    private BigDecimal withdraw;

    private Integer tenantId;

    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
