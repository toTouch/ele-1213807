package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@TableName("t_franchisee_amount")
public class FranchiseeAmount {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private Long uid;

    private Long franchiseeId;

    private Long createTime;

    private Long updateTime;

    private Double totalIncome;

    private Double balance;

    private Double withdraw;

    private Integer tenantId;

    private Integer delFlag;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
